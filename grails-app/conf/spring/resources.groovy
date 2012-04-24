import redis.clients.jedis.JedisPool

// Place your Spring DSL code here
beans = {

    redisPool1(JedisPool, ref('redisPoolConfig'), 'localhost', 6379, 2000, '' ) { bean ->
        bean.destroyMethod = 'destroy'
    }

    redisPool2(JedisPool, ref('redisPoolConfig'), 'localhost', 6380, 2000, '' ) { bean ->
        bean.destroyMethod = 'destroy'
    }

    redisPool3(JedisPool, ref('redisPoolConfig'), 'localhost', 6381, 2000, '' ) { bean ->
        bean.destroyMethod = 'destroy'
    }
}
