package com.reviewkiwi.common.util

trait UniquifyVerb {

  implicit def list2uniquifiable[A](list: List[A]) = new UniquifiableList(list)

  class UniquifiableList[A](list: List[A]) {
    def uniquified: List[A] = UniquifyVerb.uniquifyBy(list, (a: A) => a)
    def uniquifyOn[B](key: A => B): List[A] = UniquifyVerb.uniquifyBy[A, B](list, key)
    def uniquifyOn[B](key: A => B, onlyIf: A => Boolean): List[A] = UniquifyVerb.uniquifyBy[A, B](list, key, onlyIf)
    def uniquifyByMerge[B](onKey: A => B)(merge: (A, A) => A): List[A] = UniquifyVerb.uniquifyByMerge[A, B](list)(onKey, merge)
  }

  def uniquify[A](list: List[A]): List[A] =
    uniquifyBy(list, { a: A => a })

  /**
   * Create a unique copy of the given list. Uniqueness is determined by the `onKey` predicate.
   *
   * You should NOT depend on imlpementation details about which (first? last?) item will be kept
   * in the output collection.
   */
  def uniquifyBy[A, B](list: List[A], key: A => B): List[A] = {
    (Map[B, A]() ++ list.map(el => (key(el) -> el))).values.toList
  }

  def uniquifyBy[A, B](list: List[A], key: A => B, onlyIf: A => Boolean): List[A] = {
    var c = 0
    (Map[B, A]() ++ list.map(el => {
      val theKey = key(el)
      val include = onlyIf(el)
      if (include) {
        (0, theKey) -> el
      } else {
        c += 1
        (c, theKey) -> el
      }
    })).values.toList
  }

  def uniquifyByMerge[A, B](list: List[A])(onKey: A => B, merge: (A, A) => A): List[A] = {
    val uniques = uniquifyBy(list, onKey)
    val uniqueKeys = uniques.map(onKey).toSet

    if (uniqueKeys.size == list.size) {
      return uniques
    }

    // todo not preformant alg
    val notDuplicatedElements = uniques.filter(a => list.count(k => onKey(k) == onKey(a)) == 1).map(a => (onKey(a), a))
    val res = collection.mutable.HashMap[B, A](notDuplicatedElements: _*)

    for(u <- uniqueKeys; if list.count(a => onKey(a) == u) > 1) { // if there are duplicates for this key
      val dups = list.filter(a => onKey(a) == u)
      res(u) = dups.drop(1).foldLeft(dups.head)(merge)
    }

    res.values.toList
  }

}

object UniquifyVerb extends UniquifyVerb
