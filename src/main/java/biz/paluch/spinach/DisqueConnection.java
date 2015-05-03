package biz.paluch.spinach;

import com.lambdaworks.redis.RedisConnection;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueConnection<K, V> extends RedisConnection<K, V> {

}
