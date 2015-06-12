import dal.Tables
import Tables._
import Tables.profile.simple._
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import autodal.SlickPostgresDriver.simple._
import slick.jdbc.meta.MTable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ModelSpec extends Specification {
  val db = Database.forConfig("devdb")
  implicit val session: Session = db.createSession()

  "Core Tables" should {
    "be created" in {

      val getTables = MTable.getTables(None, Some("public"), None, None).map { ts =>
        ts.map { t =>
          t.name.name
        }
      }

      val requiredTables: Seq[String] = Seq(
        "data_table",
        "things_thing",
        "events_event",
        "people_person",
        "locations_location"
      )

      val tables = db.run(getTables)
      tables must containAllOf[String](requiredTables).await
    }
  }

  sequential

  "Data tables" should {
    "be empty" in {
      val result = DataValue.run
      result must have size(0)
    }
    "accept data" in {
      val dataTableRow = new DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "test", false, "test")
      val tableId = (DataTable returning DataTable.map(_.id)) += dataTableRow

      val dataFieldRow = new DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), tableId, "Test")
      val fieldId = (DataField returning DataField.map(_.id)) += dataFieldRow

      val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test")
      val recordId = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow

      // The ID value is actually ignored and auto-incremented
      val dataRow = new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test", fieldId, recordId)
      DataValue += dataRow

      val result = DataValue.run
      result must have size(1)
    }
    "allow data to be removed" in {
      DataValue.delete
      DataValue.run must have size(0)

      DataRecord.delete
      DataRecord.run must have size(0)

      DataField.delete
      DataField.run must have size(0)

      DataTable.delete
      DataTable.run must have size(0)
    }
  }

  "Facebook data structures" should {
    "have virtual tables created" in {
      val dataTableRows = Seq(
        new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "events", false, "facebook"),
        new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "me", false, "facebook"),
        new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", false, "facebook"),
        new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "owner", false, "facebook"),
        new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "place", false, "facebook"),
        new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "location", false, "facebook")
      )

      DataTable ++= dataTableRows

      val result = DataTable.filter(_.sourceName.startsWith("facebook")).run
      result must have size(6)
    }

    "have fields created and linked to the right tables" in {
      val findTableId = DataTable.filter(_.sourceName === "facebook")

      val eventsId = findTableId.filter(_.name === "events").map(_.id).run.head
      val coverId = findTableId.filter(_.name === "cover").map(_.id).run.head
      val ownerId = findTableId.filter(_.name === "owner").map(_.id).run.head
      val placeId = findTableId.filter(_.name === "place").map(_.id).run.head
      val locationId = findTableId.filter(_.name === "location").map(_.id).run.head

      val dataFieldRows = Seq(
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "attending_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "attending_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "cover"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "declined_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "description"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "end_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "feed_targeting"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "invited_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "is_date_only"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "maybe_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "name"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "noreply_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), ownerId, "owner"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "parent_group"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), placeId, "place"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "privacy"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "rvsp_status"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "start_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "end_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "ticket_url"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "timezone"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "updated_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "cover_id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "offset_x"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "offset_y"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "source"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), ownerId, "id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), ownerId, "name"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), placeId, "name"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "location"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "city"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "country"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "longitude"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "latitude"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "street"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "zip"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), placeId, "id")
      )
      DataField ++= dataFieldRows

      val result = DataField.run
      result must have size(39)
    }

    "auto-increment record rows" in {
      val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent1")
      val recordId = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow

      val dataRecordRow2 = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent2")
      val recordId2 = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow2
      recordId2 must beEqualTo(recordId + 1)
    }

    "be cleaned up on completion" in {
      //DataValue.delete
      //DataValue.run must have size(0)

      DataRecord.delete
      DataRecord.run must have size(0)

      DataField.delete
      DataField.run must have size(0)

      DataTable.filter(_.sourceName.startsWith("facebook")).delete
      DataTable.filter(_.sourceName.startsWith("facebook")).run must have size(0)
    }
  }
}