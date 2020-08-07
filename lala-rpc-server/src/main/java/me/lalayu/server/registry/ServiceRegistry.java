package me.lalayu.server.registry;

import cn.hutool.core.util.IdUtil;
import me.lalayu.zookeeper.CuratorClient;
import me.lalayu.zookeeper.ZkConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 **/
public class ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private final CuratorClient curatorClient;
    private final List<String> pathList;

    public ServiceRegistry(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress);
        this.pathList = new ArrayList<>();
    }

    public void registerService(String address) {
        if (address != null) {
            String uuid = IdUtil.objectId();
            String serviceData = uuid + ":" + address;
            byte[] bytes = serviceData.getBytes();
            try {
                String path = ZkConstant.ZK_DATA_PATH + "-" + uuid;
                this.curatorClient.createPathData(path, bytes);
                pathList.add(path);
                LOGGER.info("Register new service {}", address);
            } catch (Exception e) {
                LOGGER.error("Register service {} failed, exception: {}", address, e.getMessage());
            }
        }
    }

    public void unregisterService() {
        LOGGER.info("Unregister all service");
        for (String path : pathList) {
            try {
                this.curatorClient.deletePath(path);
            } catch (Exception e) {
                LOGGER.error("Delete service path error: {}", e.getMessage());
            }
        }
        this.curatorClient.close();
    }
}
