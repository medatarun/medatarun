package io.medatarun.tags.core.domain

interface TagCmds {
    fun dispatch(cmd: TagCmd)
}