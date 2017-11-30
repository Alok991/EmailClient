import javax.mail.internet.InternetAddress

import akka.actor.{Actor, ActorSystem, Props}
import courier.{Envelope, Mailer, Text}



import io.StdIn._
import scala.collection.mutable.ListBuffer


case class EmailWorkList(emailsToBeDone : Array[Email])
class SupervisorActor extends  Actor{
  def receive = {
    case EmailWorkList(emailsToBeDone) =>{
      //Send these emails to worker Actor
      println("Sending work to workers"+ emailsToBeDone.toString)
      emailsToBeDone.foreach{
        email => context.actorOf(Props[WorkerActors]) ! EmailWork(email)
      }
    }
    case _ =>{
      println("Something went wrong, Recieved :" + _)
    }
  }
}


case class EmailWork(email: Email)
class WorkerActors extends Actor{

  def receive = {
    case EmailWork(email) => {
      sendEmail(email)
      println("Done the work for email "+ email.toString)
    }
    case _ => {
      println("Soemthing went wriong in worker actor Recieved : "+ _)
    }
  }



  val LoginEmail = "akkaemailapp@gmail.com"
  val LoginPassword = "somedummypassword"


  def sendEmail(email: Email) = {

    import akka.dispatch.ExecutionContexts._
    implicit val ec = global
    val mailer = Mailer("smtp.gmail.com", 587)
      .auth(true)
      .as(LoginEmail, LoginPassword)
      .startTtls(true)()

    val envelope = Envelope.from(new InternetAddress(LoginEmail))
      .to(email.ToList:_*)
      .cc(email.CCList:_*)
      .bcc(email.BCCList:_*)
      .subject(email.EmailSubject)
      .content(email.EmailBody)

    mailer(envelope).onSuccess {
      case _ => println("message delivered")
    }
  }
}


class Email(toList :Array[String], ccList : Array[String], bccList : Array[String], emailSubject : String ,emailBody : String){
  val ToList = toList.map(new InternetAddress(_))
  val CCList = ccList.map(new InternetAddress(_))
  val BCCList = bccList.map(new InternetAddress(_))
  val EmailSubject = emailSubject
  val EmailBody = Text(emailBody)
}


object App {

  def main(args: Array[String]): Unit = {

    println("Welcome to Email App using Scala, Akka and courier library for mailing !!")

    println("Do you want to use a CSV file containing emails ?")
    print("Type Y or y for yes, anything else for no and press ENTER : ")

    var allEmails = new ListBuffer[Email]

    val csv_Y = readLine().matches("[Yy]")
    if(csv_Y){
      // user wants to give a csv file
      println("Using CSV file (EMAIL_DETAILS.csv) in root folder of this project")

      try{
        val lines = io.Source.fromFile("EMAIL_DETAILS.csv").getLines.drop(1)
        for (line <- lines) {
          val info = line.split(",")
          val e = makeEmail(info(0), info(1), info(2), info(3), info(4))
          allEmails += e
        }
      }
      catch {
        case e: Exception => println("Check if the file EMAIL_DETAILS.csv is present and is in correct format in ROOT DIR")
      }




    }
    else{
      println("Type the number of mails you want to send")
      val N = readInt()
      for(i <- 1 to N) {
        println("Type the To address, multiple address separated by ;")
        val TO = readLine()

        println("Type the CC address, multiple address separated by ;")
        val CC = readLine()

        println("Type the BCC address, multiple address separated by ;")
        val BCC = readLine()

        println("Type Subject")
        val Subject = readLine()

        println("Type Email Body")
        val Body = readLine()

        val e = makeEmail(TO,CC,BCC,Subject,Body)
        allEmails+=e
      }

    }



    val system = ActorSystem("EmailSystem")
    val supervisorActor = system.actorOf(Props[SupervisorActor])


    if(allEmails.size>0){
      val someDummyWork = new EmailWorkList(allEmails.toArray)
      supervisorActor ! someDummyWork
    }
    else{
      println("No mails to be sent")
    }



  }

  def makeEmail(to:String, cc: String, bcc:String, subject:String, emailbody:String): Email = {
    //TO,CC,BCC,Subject,Email_Content
    var TO : Array[String]= Array()
    var CC : Array[String] = Array()
    var BCC : Array[String] = Array()
    if(!to.split(";")(0).equals(""))
      TO=to.split(";")

    if(!cc.split(";")(0).equals(""))
      CC=cc.split(";")

    if(!bcc.split(";")(0).equals(""))
      BCC=bcc.split(";")

    val Subject = subject
    val body = emailbody

    val e = new Email(TO,CC,BCC,Subject,body)
    return e
  }

}