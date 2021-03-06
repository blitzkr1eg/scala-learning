package adrian.handsonscala

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.collection.mutable

object Trie extends App {

  case class Node(
    var hasValue: Boolean,
    var children: mutable.Map[Char, Node] = mutable.HashMap()
  )

  case class Trie(root: Node) {

    def add(word: String): Trie = {
      var current = root
      for (c <- word)
        current = current.children
          .getOrElseUpdate(c, Node(hasValue = false, children = mutable.HashMap()))
      current.hasValue = true
      this
    }

    def exists(word: String): Boolean = {
      var current = Option(root)
      for (c <- word if current.isDefined)
        current = current.get.children.get(c)
      current.exists(_.hasValue)
    }

    def prefixesMatchingString(s: String): Seq[String] = {
      var current = Option(root)
      val r: mutable.ArrayBuilder[String] = Array.newBuilder[String]

      for ((c, i) <- s.zipWithIndex if current.isDefined) {
        if (current.exists(_.hasValue)) r += s.substring(0, i)
        current = current.get.children.get(c)

      }

      if (current.exists(_.hasValue)) r += s.substring(0, s.length)
      r.result()
    }



    def stringsMatchingPrefix(prefix: String): Seq[String] = {
      var base = Option(root)
      for (c <- prefix) base = base.get.children.get(c)
      base.map(b => {
//       search(b, prefix)
        if (b.hasValue)
          searchTR(mutable.ArrayDeque(b.children.toList), prefix, Seq(prefix), prefix, Queue(0))
        else
          searchTR(mutable.ArrayDeque(b.children.toList), prefix, Seq(), prefix, Queue(0))
      }).getOrElse(Seq.empty[String])
    }

    @tailrec
    final def searchTR(remainingChildren: mutable.ArrayDeque[List[(Char, Node)]],
                       word: String, result: Seq[String], basePrefix: String, stack: Queue[Int]): Seq[String] = {

      remainingChildren match {
        case levels if levels.isEmpty => result

        case levels =>
          levels.head match {
            case (nextC, nextNode) :: Nil =>
              searchTR(
                remainingChildren.tail.prepend(nextNode.children.toList),
                word + nextC,
                if (nextNode.hasValue) result :+ (word + nextC) else result,
                basePrefix,
                if (nextNode.children.toList.length > 1) stack.appended(0) else stack.updated(stack.size - 1, stack.last + 1),
              )

            case (nextC, nextNode) :: remainingNodes =>
                searchTR(
                  remainingChildren.tail.prepend(nextNode.children.toList).append(remainingNodes),
                  word + nextC,
                  if (nextNode.hasValue) result :+ (word + nextC) else result,
                  basePrefix,
                  if (nextNode.children.toList.length > 1) stack.appended(0) else stack.updated(stack.size - 1, stack.last + 1),
                )




            case Nil => //if (
//              println(stack)// saved.isEmpty)
//              println(">>" + word + " - " + word.dropRight(stack.last))

                searchTR(remainingChildren.tail,
                  word.dropRight(stack.last),
                  result, basePrefix, stack.init match {
                    case x if x.isEmpty=> Queue(0)
                    case r => r
                  }
                )
            //else  searchTR(remainingChildren, basePrefix + saved.head, result, basePrefix, saved.tail)
          }

      }

    }

    def search(base: Node, prefix: String): Seq[String] = {
      val ir = {
        val x = for ((c, node) <- base.children) yield {
          search(node, prefix + c)
        }
        x.toSeq.flatten
      }
      val r = if (base.hasValue)
        Seq(prefix) ++ ir
      else ir
      r
    }
  }

  var t = Trie(Node(hasValue = false))
  t.add("mandarin").add("man").add("map").add("mango").add("maABC")

  assert(t.exists("mango"))
  assert(t.exists("mandarin"))
  assert(t.exists("map"))
  assert(t.exists("man"))
  assert(t.exists("maABC"))

  assert(!t.exists("manx"))
  assert(!t.exists("ma"))

  println("prefixes for mango: " + t.prefixesMatchingString("mango"))
  println("prefixes for lol: " + t.prefixesMatchingString("lol"))


//  println("string matching prefix man: " + t.stringsMatchingPrefix("max")) // man, mandarin, mango

  assert(!t.exists("mago"))
  println("string matching prefix ma: " + t.stringsMatchingPrefix("ma")) // man, map, mandarin, mango

//  println("string matching prefix map: " + t.stringsMatchingPrefix("map")) // map
//  println("string matching prefix mando: " + t.stringsMatchingPrefix("mando")) // ""
//  println("string matching prefix mand: " + t.stringsMatchingPrefix("mand")) // mandarin

  println("done")

}
