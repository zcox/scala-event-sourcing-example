package com.zilverline.es2
package reports

import events._

import scala.collection._

trait Document {
  def applyEvent: CommittedEvent => Document
}

class Documents {
  def investigate[T](investigator: Investigator[T])(implicit m: Manifest[T]) {
    investigators.put(m.erasure, investigator)
  }

  def update(event: CommittedEvent) {
    reports.get(event.source) foreach {
      report => reports.put(event.source, report.applyEvent(event))
    }
    investigators.get(event.payload.getClass)
      .flatMap(_.asInstanceOf[Investigator[AnyRef]].lift.apply(event))
      .foreach(report => reports.put(event.source, report))
  }

  def store(source: Identifier, document: Document) {
    reports.put(source, document)
  }

  def retrieve[T <: Document](source: Identifier) = reports(source)

  private val investigators: mutable.Map[Class[_], Investigator[_]] = mutable.Map.empty
  private val reports: mutable.Map[Identifier, Document] = mutable.Map.empty
}
