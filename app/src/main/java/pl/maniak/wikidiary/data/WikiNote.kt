package pl.maniak.wikidiary.data

import java.util.Date

data class WikiNote(val id:Long, val tag:String, val description:String,val date:Date, val isSend:Boolean)