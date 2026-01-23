import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey

sealed interface EntityRef {

    data class ById(
        val id: EntityId
    ) : EntityRef

    data class ByKey(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
    ) : EntityRef
}