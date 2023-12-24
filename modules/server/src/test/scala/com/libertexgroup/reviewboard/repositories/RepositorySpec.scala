package com.libertexgroup.reviewboard.repositories

import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import zio.*
import javax.sql.DataSource

trait RepositorySpec {
  private def createContainer(): PostgreSQLContainer[Nothing] = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript("sql/companies.sql") // src/test/resources
    container.start()
    container
  }

  // create a DataSource to connect to the PG
  private def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(container.getJdbcUrl())
    dataSource.setUser(container.getUsername())
    dataSource.setPassword(container.getPassword())
    dataSource
  }

  // use the DataSource to build th Quill instance as a ZLayer
  val dataSourceLayer = ZLayer {
    for {
      container <- ZIO.acquireRelease(ZIO.attempt(createContainer()))(container =>
        ZIO.attempt(container.stop()).ignoreLogged
      )
      dataSource <- ZIO.attempt(createDataSource(container))
    } yield dataSource
  }

}
