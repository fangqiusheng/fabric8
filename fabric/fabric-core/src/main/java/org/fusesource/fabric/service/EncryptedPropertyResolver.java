/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.service;

import static org.fusesource.fabric.zookeeper.ZkPath.AUTHENTICATION_CRYPT_ALGORITHM;
import static org.fusesource.fabric.zookeeper.ZkPath.AUTHENTICATION_CRYPT_PASSWORD;
import static org.fusesource.fabric.zookeeper.utils.ZooKeeperUtils.getStringData;

import org.apache.curator.framework.CuratorFramework;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.fusesource.fabric.api.FabricException;
import org.fusesource.fabric.api.PlaceholderResolver;
import org.fusesource.fabric.service.support.AbstractComponent;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.osgi.service.component.ComponentContext;

@Component(name = "org.fusesource.fabric.placholder.resolver.crypt", description = "Fabric Encrypted Property Placeholder Resolver")
@Service(PlaceholderResolver.class)
public class EncryptedPropertyResolver extends AbstractComponent implements PlaceholderResolver {

    private static final String CRYPT_SCHEME = "crypt";

    @Reference(referenceInterface = CuratorFramework.class)
    private CuratorFramework curator;

    @Activate
    synchronized void activate(ComponentContext context) {
        activateComponent(context);
    }

    @Deactivate
    synchronized void deactivate() {
        deactivateComponent();
    }

    @Override
    public String getScheme() {
        return CRYPT_SCHEME;
    }

    @Override
    public synchronized String resolve(String pid, String key, String value) {
        return getEncryptor().decrypt(value.substring(CRYPT_SCHEME.length() + 1));
    }

    private PBEStringEncryptor getEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(getAlgorithm());
        encryptor.setPassword(getPassword());
        return encryptor;
    }

    private String getAlgorithm() {
        try {
            return getStringData(curator, AUTHENTICATION_CRYPT_ALGORITHM.getPath());
        } catch (Exception e) {
            throw new FabricException(e);
        }
    }

    private String getPassword() {
        try {
            return getStringData(curator, AUTHENTICATION_CRYPT_PASSWORD.getPath());
        } catch (Exception e) {
            throw new FabricException(e);
        }
    }

    public CuratorFramework getCurator() {
        return curator;
    }

    public void setCurator(CuratorFramework curator) {
        this.curator = curator;
    }
}
