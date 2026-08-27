package br.com.tiago.obramaster.data.db

fun createDatabase(driverFactory: DatabaseDriverFactory): ObraMasterDatabase =
    ObraMasterDatabase(driverFactory.create())
