import json
import re
from pathlib import Path
from urllib.request import urlopen


def load_json(base_url: str, path: str):
    with urlopen(f"{base_url}{path}") as response:
        return json.load(response)


def format_typescript_object(value) -> str:
    value_json = json.dumps(value, indent=2, ensure_ascii=False)
    return re.sub(r'"([A-Za-z_][A-Za-z0-9_]*)":', r"\1:", value_json)


def write_action_registry(base_url: str, target_file: Path) -> None:
    items = load_json(base_url, "config/inspect_actions_all")
    items_json = format_typescript_object(items)
    content = (
        'import type { ActionRegistryDto } from "@medatarun/ui/business/action-registry";\n'
        "\n"
        "/** This file is auto-generated from the ActionRegistry backend. Do not modify.*/"
        "\n"
        f"export const prefetch_inspect_action_registry_all = {items_json} as const satisfies ActionRegistryDto;\n"
    )

    target_file.write_text(content, encoding="utf-8")


def write_inspect_type_system(base_url: str, target_file: Path) -> None:
    type_system = load_json(base_url, "config/inspect_type_system")
    type_system_json = format_typescript_object(type_system)
    content = (
        'import type { TypeDescriptorDto } from "@medatarun/ui/business/types/TypeDescriptorDto.ts";\n'
        "\n"
        "/** This file is auto-generated from the TypeDescriptor backend. Do not modify.*/"
        "\n"
        f"export const prefetch_inspect_type_system: {{ items: TypeDescriptorDto[] }} = {type_system_json};\n"
    )

    target_file.write_text(content, encoding="utf-8")


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    repo_dir = script_dir.parent.parent.parent
    base_url = "http://localhost:8080/api/"
    app_name = "medatarun"

    write_action_registry(
        base_url,
        repo_dir / f"ui/src/app-{app_name}/generated-action-registry.ts",
    )
    write_inspect_type_system(
        base_url,
        repo_dir / f"ui/src/app-{app_name}/generated-type-system.ts",
    )


if __name__ == "__main__":
    main()
