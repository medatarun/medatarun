import path from "node:path";
import {fileURLToPath} from "node:url";
import {Project} from "ts-morph";

type ModuleMapping = {
  exportNames: Set<string>;
  modulePath: string;
};

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const uiDir = path.resolve(scriptDir, "..");
const tsConfigFilePath = path.join(uiDir, "tsconfig.app.json");
const businessSubmodules = [
  "action_runner",
  "action_form",
  "action_registry",
  "model",
  "tag",
];

function getSubmoduleMappings(project: Project): ModuleMapping[] {
  const mappings: ModuleMapping[] = [];

  for (const moduleName of businessSubmodules) {
    const moduleSourceFile = project.getSourceFileOrThrow(
      path.join(uiDir, "src", "business", moduleName, "index.ts"),
    );
    const exportNames = new Set(moduleSourceFile.getExportedDeclarations().keys());

    mappings.push({
      exportNames,
      modulePath: `@/business/${moduleName}`,
    });
  }

  return mappings;
}

function getExportToModuleMap(mappings: ModuleMapping[]): Map<string, string> {
  const exportToModule = new Map<string, string>();

  for (const mapping of mappings) {
    for (const exportName of mapping.exportNames) {
      const existingModulePath = exportToModule.get(exportName);

      if (existingModulePath && existingModulePath !== mapping.modulePath) {
        throw new Error(`Export "${exportName}" exists in both "${existingModulePath}" and "${mapping.modulePath}".`);
      }

      exportToModule.set(exportName, mapping.modulePath);
    }
  }

  return exportToModule;
}

function rewriteBusinessImports(project: Project, exportToModule: Map<string, string>, moduleOrder: string[]): string[] {
  const changedFiles: string[] = [];

  for (const sourceFile of project.getSourceFiles("src/**/*.{ts,tsx}")) {
    let fileChanged = false;

    for (const importDeclaration of sourceFile.getImportDeclarations()) {
      if (importDeclaration.getModuleSpecifierValue() !== "@/business") {
        continue;
      }

      const defaultImport = importDeclaration.getDefaultImport();
      const namespaceImport = importDeclaration.getNamespaceImport();

      if (defaultImport || namespaceImport) {
        throw new Error(`Unsupported import syntax in ${sourceFile.getFilePath()}`);
      }

      const namedImports = importDeclaration.getNamedImports();
      const groupedImports = new Map<string, typeof namedImports>();

      for (const namedImport of namedImports) {
        const exportName = namedImport.getNameNode().getText();
        const targetModulePath = exportToModule.get(exportName);

        if (!targetModulePath) {
          throw new Error(`No submodule export found for "${exportName}" in ${sourceFile.getFilePath()}`);
        }

        const currentGroup = groupedImports.get(targetModulePath) ?? [];
        currentGroup.push(namedImport);
        groupedImports.set(targetModulePath, currentGroup);
      }

      const insertIndex = sourceFile.getImportDeclarations().indexOf(importDeclaration);
      const orderedGroups = [...groupedImports.entries()].sort(([left], [right]) => {
        return moduleOrder.indexOf(left) - moduleOrder.indexOf(right);
      });

      sourceFile.insertImportDeclarations(
        insertIndex,
        orderedGroups.map(([modulePath, imports]) => {
          const allTypeOnly = imports.every((namedImport) => namedImport.isTypeOnly());

          return {
            isTypeOnly: allTypeOnly,
            moduleSpecifier: modulePath,
            namedImports: imports.map((namedImport) => ({
              alias: namedImport.getAliasNode()?.getText(),
              isTypeOnly: allTypeOnly ? false : namedImport.isTypeOnly(),
              name: namedImport.getNameNode().getText(),
            })),
          };
        }),
      );

      importDeclaration.remove();
      fileChanged = true;
    }

    if (fileChanged) {
      changedFiles.push(path.relative(uiDir, sourceFile.getFilePath()));
    }
  }

  return changedFiles;
}

function main() {
  const dryRun = process.argv.includes("--dry-run");
  const project = new Project({
    tsConfigFilePath,
  });
  const mappings = getSubmoduleMappings(project);
  const exportToModule = getExportToModuleMap(mappings);
  const moduleOrder = mappings.map((mapping) => mapping.modulePath);
  const changedFiles = rewriteBusinessImports(project, exportToModule, moduleOrder);

  if (changedFiles.length === 0) {
    console.log("No imports to rewrite.");
    return;
  }

  if (dryRun) {
    console.log("Files that would be updated:");
  } else {
    project.saveSync();
    console.log("Updated files:");
  }

  for (const changedFile of changedFiles) {
    console.log(changedFile);
  }
}

main();
