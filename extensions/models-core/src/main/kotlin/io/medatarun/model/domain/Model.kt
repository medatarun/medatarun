package io.medatarun.model.domain

import java.net.URL

interface Model {

    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: ModelId

    /**
     * Unique key of the model accros all models managed by the current application instance
     */
    val key: ModelKey

    /**
     * Display name of the model
     */
    val name: LocalizedText?

    /**
     * Display description of the model
     */
    val description: LocalizedMarkdown?

    /**
     * Version of the model
     */
    val version: ModelVersion

    /**
     * Origin of the model, either created by the application or imported from another source
     */
    val origin: ModelOrigin

    /**
     * Authority level of the model. System models usually come from concrete implementations,
     * canonical models are reference targets maintained by users and serve as business reference.
     */
    val authority: ModelAuthority

    /**
     * Documentation home
     */
    val documentationHome: URL?

}
