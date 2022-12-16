package com.main.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

public class CommonUtil {

    private CommonUtil() {
    }

    public static String getServiceIdByUri(String uri) {
        String regularUrl = "^(\\/[\\d\\D]*){2}[\\d\\D]*";
        Pattern pattern = Pattern.compile(regularUrl);
        boolean flag = pattern.matcher(uri).matches();
        return flag ? uri.substring(1, uri.indexOf("/", uri.indexOf("/") + 1)) : "";
    }

    public static Map<String, String> getSecurityInfo(DiscoveryClient factory, String path) {
        Map<String, String> map = new HashMap();
        String serviceId = getServiceIdByUri(path);
        if (StringUtils.isNotBlank(serviceId)) {
            List<ServiceInstance> instance = factory.getInstances(serviceId);
            Iterator var5 = instance.iterator();

            while(var5.hasNext()) {
                ServiceInstance serviceInstance = (ServiceInstance)var5.next();
                map = serviceInstance.getMetadata();
                if (!((Map)map).isEmpty()) {
                    break;
                }
            }
        }

        return (Map)map;
    }

    public static String getBase64Credentials(String username, String password) {
        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        return new String(base64CredsBytes);
    }
}
