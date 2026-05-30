package io.medatarun.model.infra.db

import io.medatarun.model.ports.needs.ModelStorageCmdAnyVersion
import io.medatarun.storage.eventsourcing.testkit.StorageCmdTestCase

typealias CmdTestCase = StorageCmdTestCase<ModelStorageCmdAnyVersion>
