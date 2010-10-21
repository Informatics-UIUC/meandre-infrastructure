package meandre.kernel.state

import com.mongodb.Mongo

/**
 * @author Boris Capitanu
 */

object MongoPool {
  var mongoPool:Map[String, Mongo] = Map()

  def inject(mongo:Mongo, key:String) = mongoPool += key -> mongo
  
  def apply(key:String) = mongoPool get key
}
