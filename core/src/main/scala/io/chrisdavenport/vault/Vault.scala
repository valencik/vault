package io.chrisdavenport.vault

import io.chrisdavenport.unique.Unique
/**
 * Vault - A persistent store for values of arbitrary types.
 * This extends the behavior of the locker, into a Map
 * that maps Keys to Lockers, creating a heterogenous
 * store of values, accessible by keys. Such that the Vault
 * has no type information, all the type information is contained 
 * in the keys.
 */
final class Vault private (private val m: Map[Unique, Locker]) {
  def empty : Vault = Vault.empty
  def lookup[A](k: Key[A]): Option[A] = Vault.lookup(k, this)
  def insert[A](k: Key[A], a: A): Vault = Vault.insert(k, a, this)
  def delete[A](k: Key[A]): Vault = Vault.delete(k, this)
  def adjust[A](k: Key[A], f: A => A): Vault = Vault.adjust(k, f, this)
  def ++(that: Vault): Vault = Vault.union(this, that)
}
object Vault {
  /**
   * The Empty Vault 
   */
  def empty = new Vault(Map.empty)

  /**
   * Lookup the value of a key in the vault
   */
  def lookup[A](k: Key[A], v: Vault): Option[A] = 
    v.m.get(k.unique).flatMap(Locker.unlock(k, _))
  
  /**
   * Insert a value for a given key. Overwrites any previous value.
   */
  def insert[A](k: Key[A], a: A, v: Vault): Vault = 
    new Vault(v.m + (k.unique -> Locker.lock(k, a)))
  
  /**
   * Delete a key from the vault
   */
  def delete[A](k: Key[A], v: Vault): Vault = 
    new Vault(v.m - k.unique)
  
  /**
   * Adjust the value for a given key if it's present in the vault.
   */
  def adjust[A](k: Key[A], f: A => A, v: Vault): Vault = 
    lookup(k, v).fold(v)(a => insert(k, f(a), v))

  /**
   * Merge Two Vaults
   */
  def union(v1: Vault, v2: Vault): Vault = 
    new Vault(v1.m ++ v2.m)

}