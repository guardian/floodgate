package com.gu.floodgate.jobhistory

import scala.concurrent.Future

class JobHistoryService {

  def getJobHistories(): Future[List[JobHistory]] = ???
  def getJobHistory(id: String): Future[Option[JobHistory]] = ???

}
