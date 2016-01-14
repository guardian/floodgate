package com.gu.floodgate.runningjob

import scala.concurrent.Future

class RunningJobService {

  def getRunningJobs(): Future[List[RunningJob]] = ???
  def getRunningJob(id: String): Future[Option[RunningJob]] = ???

}
