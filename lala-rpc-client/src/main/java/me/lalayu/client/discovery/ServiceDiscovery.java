package me.lalayu.client.discovery;

import com.google.common.collect.Lists;
import me.lalayu.zookeeper.CuratorClient;
import me.lalayu.zookeeper.ZkConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 **/
public class ServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private final CuratorClient curatorClient;

    public ServiceDiscovery(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress);
        discoveryService();
    }

    private void discoveryService() {
        try {
            refreshServiceSever();

            curatorClient.watchPathChildrenNode(ZkConstant.ZK_REGISTRY_PATH, new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                    PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                    if (type == PathChildrenCacheEvent.Type.CHILD_ADDED || type == PathChildrenCacheEvent.Type.CHILD_UPDATED
                            || type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {

                        refreshServiceSever();
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Watch node exception:" + e.getMessage());
        }
    }

    private void refreshServiceSever() {
        try {
            List<String> nodeList = curatorClient.getChildren(ZkConstant.ZK_REGISTRY_PATH);
            List<String> dataList = Lists.newArrayList();
            for (String node : nodeList) {
                LOGGER.info("Service node: " + node);
                byte[] bytes = curatorClient.getData(ZkConstant.ZK_REGISTRY_PATH + "/" + node);
                dataList.add(new String(bytes));
            }
            LOGGER.debug("Node data:{}", dataList);
            LOGGER.debug("Service discovery triggered updating connected server node.");
            ConnectManager.getInstance().updateConnectSever(dataList);
        } catch (Exception e) {
            LOGGER.error("failed to get Node, " + e.getMessage());
        }
    }

    public void stop() {
        this.curatorClient.close();
    }
}
