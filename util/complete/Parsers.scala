/* sbt -- Simple Build Tool
 * Copyright 2011  Mark Harrah
 */
package sbt.complete

	import Parser._
	import java.io.File
	import java.net.URI
	import java.lang.Character.{getType, MATH_SYMBOL, OTHER_SYMBOL, DASH_PUNCTUATION, OTHER_PUNCTUATION, MODIFIER_SYMBOL, CURRENCY_SYMBOL}

// Some predefined parsers
trait Parsers
{
	lazy val any: Parser[Char] = charClass(_ => true)

	lazy val DigitSet = Set("0","1","2","3","4","5","6","7","8","9")
	lazy val Digit = charClass(_.isDigit) examples DigitSet
	lazy val Letter = charClass(_.isLetter)
	def IDStart = Letter
	lazy val IDChar = charClass(isIDChar)
	lazy val ID = IDStart ~ IDChar.* map { case x ~ xs => (x +: xs).mkString }
	lazy val OpChar = charClass(isOpChar)
	lazy val Op = OpChar.+.string
	lazy val OpOrID = ID | Op

	def isOpChar(c: Char) = !isDelimiter(c) && isOpType(getType(c))
	def isOpType(cat: Int) = cat match { case MATH_SYMBOL | OTHER_SYMBOL | DASH_PUNCTUATION | OTHER_PUNCTUATION | MODIFIER_SYMBOL | CURRENCY_SYMBOL => true; case _ => false }
	def isIDChar(c: Char) = c.isLetterOrDigit || c == '-' || c == '_'
	def isDelimiter(c: Char) = c match { case '`' | '\'' | '\"' | /*';' | */',' | '.' => true ; case _ => false }
	
	lazy val NotSpaceClass = charClass(!_.isWhitespace)
	lazy val SpaceClass = charClass(_.isWhitespace)
	lazy val NotSpace = NotSpaceClass.+.string
	lazy val Space = SpaceClass.+.examples(" ")
	lazy val OptSpace = SpaceClass.*.examples(" ")

	// TODO: implement
	def fileParser(base: File): Parser[File] = token(mapOrFail(NotSpace)(s => new File(s.mkString)), "<file>")
	
	lazy val Port = token(IntBasic, "<port>")
	lazy val IntBasic = mapOrFail( '-'.? ~ Digit.+ )( Function.tupled(toInt) )
	private[this] def toInt(neg: Option[Char], digits: Seq[Char]): Int =
		(neg.toSeq ++ digits).mkString.toInt

	def mapOrFail[S,T](p: Parser[S])(f: S => T): Parser[T] =
		p flatMap { s => try { success(f(s)) } catch { case e: Exception => failure(e.toString) } }

	def spaceDelimited(display: String): Parser[Seq[String]] = (token(Space) ~> token(NotSpace, display)).* <~ SpaceClass.*

	def trimmed(p: Parser[String]) = p map { _.trim }
	def Uri(ex: Set[URI]) = mapOrFail(NotSpace)( uri => new URI(uri)) examples(ex.map(_.toString))
}
object Parsers extends Parsers
object DefaultParsers extends Parsers with ParserMain