package io.medatarun.tags.core.adapters

import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagFree
import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagManaged
import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagManagedKey
import io.medatarun.tags.core.ports.needs.TagRepoCmd
import io.medatarun.tags.core.ports.needs.TagStorage
import org.intellij.lang.annotations.Language

class TagStorageSQLite(private val dbConnectionFactory: DbConnectionFactory): TagStorage {

    fun initSchema() {
        dbConnectionFactory.getConnection().use { connection ->
            SCHEMA.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { stmt ->
                    connection.createStatement().execute(stmt)
                }
        }
    }

    override fun findTagFreeByKeyOptional(key: TagFreeKey): TagFree? {
        TODO("Not yet implemented")
    }

    override fun findTagFreeByIdOptional(id: TagFreeId): TagFree? {
        TODO("Not yet implemented")
    }

    override fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        TODO("Not yet implemented")
    }

    override fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        TODO("Not yet implemented")
    }

    override fun findTagManagedByIdOptional(id: TagManagedId): TagManaged? {
        TODO("Not yet implemented")
    }

    override fun findTagManagedByKeyOptional(
        id: TagGroupId,
        key: TagManagedKey
    ): TagManaged? {
        TODO("Not yet implemented")
    }

    override fun dispatch(cmd: TagRepoCmd) {
        TODO("Not yet implemented")
    }

    companion object {
        @Language("SQLite")
        private const val SCHEMA = """
CREATE TABLE IF NOT EXISTS tag_free (
  id TEXT PRIMARY KEY UNIQUE,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS tag_group (
  id TEXT PRIMARY KEY UNIQUE,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS tag_managed (
  id TEXT PRIMARY KEY UNIQUE,
  tag_group_id TEXT NOT NULL,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

"""

    }

}