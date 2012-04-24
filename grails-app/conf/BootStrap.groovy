import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import grails.plugin.redis.RedisService

class BootStrap {

    def init = { servletContext ->

        RedisService.metaClass.withRedisPool = {JedisPool pool, Closure closure ->
            Jedis redis = pool.resource
            try {
                return closure(redis)
            } finally {
                pool.returnResource(redis)
            }
        }

    }
    def destroy = {
    }
}
