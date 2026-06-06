package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "literature_items")
data class LiteratureItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val summary: String,
    val lastAccessed: Long = System.currentTimeMillis(),
    val lastEdited: Long = lastAccessed,
    val imagePath: String? = null
)

val staticLiteratures = listOf(
    LiteratureItem(
        1,
        "Manifest der Kommunistischen Partei",
        "Karl Marx & Friedrich Engels",
        "Das Manifest der Kommunistischen Partei wurde 1848 veröffentlicht und ist das einflussreichste politische Programm der sozialistischen und kommunistischen Bewegung. Es beginnt mit dem berühmten Satz: „Ein Gespenst geht um in Europa – das Gespenst des Kommunismus.“ Das Werk bietet eine präzise gesellschaftliche und ökonomische Analyse der kapitalistischen Produktionsweise und postuliert, dass die gesamte bisherige Geschichte eine Geschichte von Klassenkämpfen sei. Es beschreibt, wie die Bourgeoisie alle feudalen Verhältnisse zerstört und an ihre Stelle die nackte Ausbeutung gesetzt hat. Gleichzeitig produziere sie ihre eigenen Totengräber: das Proletariat. Das Manifest endet mit dem internationalen Aufruf zur Revolution: „Proletarier aller Länder, vereinigt euch!“, und fordert die gewaltsame Überwindung der bürgerlichen Gesellschaft, die Aufhebung des Privateigentums an Produktionsmitteln und die Errichtung einer klassenlosen, kommunistischen Gesellschaft."
    ),
    LiteratureItem(
        2,
        "Das Kapital. Kritik der politischen Ökonomie",
        "Karl Marx",
        "„Das Kapital“ ist das fundamentale theoretische Hauptwerk von Karl Marx, dessen erster Band („Der Produktionsprozess des Kapitals“) 1867 erschien. Es formuliert eine radikale Kritik an der klassischen Nationalökonomie und entschlüsselt die Bewegungsgesetze der kapitalistischen Produktionsweise. Ausgangspunkt ist die Analyse der Ware und ihres Doppelcharakters von Gebrauchs- und Tauschwert. Marx deckt den Fetischcharakter der Ware auf und entwickelt die Arbeitswertlehre weiter, um die Entstehung von Mehrwert zu erklären: Der Kapitalist kauft die Arbeitskraft des Arbeiters (nicht die Arbeit) zu ihrem Reproduktionswert. In der Produktion schafft die Arbeitskraft jedoch einen Neuwert, der den Wert der Arbeitskraft (Lohn) übersteigt. Die Differenz ist der Mehrwert, den sich der Kapitalist aneignet – der Kern der Ausbeutung. Weiterhin analysiert Marx den Prozess der Akkumulation, das Gesetz des tendenziellen Falls der Profitrate und die notwendigen Krisenzyklen des Kapitalismus, die unweigerlich zu seinem Zusammenbruch führen müssten."
    ),
    LiteratureItem(
        3,
        "Lohn, Preis und Profit",
        "Karl Marx",
        "„Lohn, Preis und Profit“ basiert auf einflussreichen Reden, die Marx 1865 vor dem Generalrat der Ersten Internationale hielt. Sie gelten als leicht verständliche Einführung in die politische Ökonomie des Marxismus und die Grundgedanken des ersten Bandes des „Kapitals“. Marx tritt darin dem Argument des Gewerkschafters John Weston entgegen, der behauptete, Lohnerhöhungen seien langfristig sinnlos, da sie lediglich zu allgemeinen Preissteigerungen führten und den Reallohn der Arbeiter nicht verbesserten. Marx weist nach, dass der Wert von Waren nicht durch den Lohn, sondern durch die zur Herstellung gesellschaftlich notwendige Arbeitszeit bestimmt werde. Ein allgemeines Steigen der Löhne führe daher zu einem Fall der allgemeinen Profitrate, nicht aber zu steigenden Preisen. Gleichzeitig macht er deutlich, dass der Kampf um höhere Löhne zwar essenziell für die Erhaltung des Lebensstandards sei, jedoch eine bloße Defensivmaßnahme darstelle. Das Endziel der Arbeiterbewegung müsse die Abschaffung des Lohnsystems selbst sein."
    ),
    LiteratureItem(
        4,
        "Staat und Revolution",
        "Wladimir Lenin",
        "„Staat und Revolution“ entstand 1917 am Vorabend der Oktoberrevolution und stellt Lenins zentrales staatstheoretisches Werk dar. Er wendet sich darin polemisch gegen reformistische und sozialdemokratische Strömungen, die annahmen, man könne den bürgerlichen Staat auf parlamentarischem Weg allmählich in einen sozialistischen umformen. Lenin macht anhand historischer Schriften von Marx und Engels klar, dass der bürgerliche Staat immer ein Instrument der Klassenherrschaft, ein Organ zur Unterdrückung einer Klasse durch eine andere sei. Seine Hauptthese: Das Proletariat darf die fertige bürgerliche Staatsmaschine (Militär, Bürokratie) nicht einfach übernehmen, sondern muss sie zerschlagen. An ihre Stelle soll eine „Diktatur des Proletariats“ treten (nach dem Vorbild der Pariser Kommune von 1871), ein proletarischer Halbstaat, der nach Überwindung aller Klassenwidersprüche und dem Aufbau der klassenlosen Gesellschaft am Ende unweigerlich „absterben“ wird."
    )
)
