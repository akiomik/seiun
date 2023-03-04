package io.github.akiomik.seiun.model

import io.github.akiomik.seiun.model.ListRecord

data class ListRecords(val records: List<ListRecord>, val cursor: String?)
