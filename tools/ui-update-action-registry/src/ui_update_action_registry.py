import json
import re
from pathlib import Path
from urllib.request import urlopen


def main() -> None:
    with urlopen("http://localhost:8080/api/config/inspect_actions_all") as response:
        items = json.load(response)

    script_dir = Path(__file__).resolve().parent
    target_file = (
        script_dir.parent.parent.parent
        / "ui/src/business/action_registry/action_registry.static.ts"
    )

    items_json = json.dumps(items, indent=2, ensure_ascii=False, )
    items_json = re.sub(r'"([A-Za-z_][A-Za-z0-9_]*)":', r"\1:", items_json)
    content = (
        'import type { ActionRegistryDto } from "@/business/action_registry/action_registry.dto.ts";\n'
        "\n"
        f"export const actionRegistryStatic = {items_json} as const satisfies ActionRegistryDto;\n"
    )

    target_file.write_text(content, encoding="utf-8")


if __name__ == "__main__":
    main()
