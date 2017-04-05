import lovelace.util.common {
    todo
}
"An exception to warn about duplicate IDs."
todo("Find a way to pass in a [[javax.xml.stream::Location]] for when this is from reading
      XML")
shared class DuplicateIDException(Integer id)
		extends Exception("Duplicate ID #``id``") {}