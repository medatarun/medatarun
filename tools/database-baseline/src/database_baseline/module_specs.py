from dataclasses import dataclass
import pathlib

@dataclass(frozen=True)
class ModuleSpec:
    name: str
    table_names: tuple[str, ...]
    output_path: pathlib.Path


MODULE_SPECS: tuple[ModuleSpec, ...] = (
    # Order is important as tables will be generated in this order
    ModuleSpec(
        name="auth",
        table_names=(
            "auth_actor",
            "auth_actor_role",
            "auth_role",
            "auth_role_permission",
            "auth_client",
            "auth_code",
            "auth_ctx",
            "users",
        ),
        output_path=pathlib.Path(
            "libs/platform-auth/src/main/resources/io/medatarun/auth/infra/db/init__auth_sqlite.sql"
        ),
    ),
    # Order is important as tables will be generated in this order
    ModuleSpec(
        name="models",
        table_names=(
            "model",
            "model_event",
            "model_snapshot",
            "model_tag_snapshot",
            "model_type_snapshot",
            "model_entity_snapshot",
            "model_entity_tag_snapshot",
            "model_entity_attribute_snapshot",
            "model_entity_attribute_tag_snapshot",
            "model_relationship_snapshot",
            "model_relationship_tag_snapshot",
            "model_relationship_attribute_snapshot",
            "model_relationship_attribute_tag_snapshot",
            "model_relationship_role_snapshot",
            "model_entity_pk_snapshot",
            "model_entity_pk_attribute_snapshot",
            "model_business_key_snapshot",
            "model_business_key_attribute_snapshot",
            "model_search_item_snapshot",
            "model_search_item_tag_snapshot",
        ),
        output_path=pathlib.Path(
            "extensions/models-core/src/main/resources/io/medatarun/model/infra/db/init__models_sqlite.sql"
        ),
    ),
    ModuleSpec(
        name="tags",
        table_names=(
            "tag_event",
            "tag_view_current_tag",
            "tag_view_current_tag_group",
            "tag_view_history_tag",
            "tag_view_history_tag_group",
        ),
        output_path=pathlib.Path(
            "extensions/tags-core/src/main/resources/io/medatarun/tags/core/infra/db/init__tags_sqlite.sql"
        ),
    ),
    ModuleSpec(
        name="actions",
        table_names=("action_audit_event",),
        output_path=pathlib.Path(
            "extensions/platform-actions-storage-db/src/main/resources/io/medatarun/actions/infra/db/init__actions_sqlite.sql"
        ),
    ),
)

SYSTEM_MAINTENANCE_ISSUER = "urn:medatarun:system"
SYSTEM_MAINTENANCE_SUBJECT = "system-maintenance"