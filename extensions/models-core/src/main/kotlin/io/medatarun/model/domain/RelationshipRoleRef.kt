import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipRoleId
import io.medatarun.model.domain.RelationshipRoleKey

sealed interface RelationshipRoleRef {

    data class ById(
        val id: RelationshipRoleId
    ) : RelationshipRoleRef

    data class ByKey(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
    ) : RelationshipRoleRef
}