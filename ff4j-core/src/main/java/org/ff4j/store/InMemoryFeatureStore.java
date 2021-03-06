package org.ff4j.store;

/*
 * #%L ff4j-core $Id:$ $HeadURL:$ %% Copyright (C) 2013 Ff4J %% Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. #L%
 */

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.ff4j.core.Feature;
import org.ff4j.core.FeatureStore;
import org.ff4j.core.FeatureXmlParser;
import org.ff4j.exception.FeatureAlreadyExistException;
import org.ff4j.exception.FeatureNotFoundException;

/**
 * Storing states of feature inmemory with initial values. Could be used mostly for testing purpose.
 * 
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 */
public class InMemoryFeatureStore implements FeatureStore {

    /** InMemory Feature Map */
    private Map<String, Feature> featuresMap = new LinkedHashMap<String, Feature>();

    /** Default. */
    public InMemoryFeatureStore() {}

    /** Default. */
    public InMemoryFeatureStore(String fileName) {
        loadConfFile(fileName);
    }

    /** Default. */
    public InMemoryFeatureStore(Map<String, Feature> maps) {
        this.featuresMap = maps;
    }

    /**
     * Load configuration through FF4J.vml file.
     * 
     * @param conf
     *            xml filename
     */
    private void loadConfFile(String conf) {
        InputStream xmlIN = getClass().getClassLoader().getResourceAsStream(conf);
        this.featuresMap = new FeatureXmlParser().parseConfigurationFile(xmlIN);
    }

    /** {@inheritDoc} */
    @Override
    public void create(Feature fp) {
        if (exist(fp.getUid())) {
            throw new FeatureAlreadyExistException(fp.getUid());
        }
        featuresMap.put(fp.getUid(), fp);
    }

    /** {@inheritDoc} */
    @Override
    public void update(Feature fp) {
        Feature fpExist = read(fp.getUid());

        // Checking new roles
        Set<String> toBeAdded = new HashSet<String>();
        toBeAdded.addAll(fp.getAuthorizations());
        toBeAdded.removeAll(fpExist.getAuthorizations());
        for (String addee : toBeAdded) {
            // Will fail if invalid userrole
            grantRoleOnFeature(fpExist.getUid(), addee);
        }

        featuresMap.put(fp.getUid(), fp);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String fpId) {
        if (!exist(fpId)) {
            // Feature cannot be deleted as does not exist
            throw new FeatureNotFoundException(fpId);
        }
        featuresMap.remove(fpId);
    }

    /** {@inheritDoc} */
    @Override
    public void grantRoleOnFeature(String flipId, String roleName) {
        if (!exist(flipId)) {
            throw new FeatureNotFoundException(flipId);
        }
        featuresMap.get(flipId).getAuthorizations().add(roleName);
    }

    /** {@inheritDoc} */
    @Override
    public void removeRoleFromFeature(String flipId, String roleName) {
        if (!exist(flipId)) {
            throw new FeatureNotFoundException(flipId);
        }
        featuresMap.get(flipId).getAuthorizations().remove(roleName);
    }

    /** {@inheritDoc} */
    @Override
    public boolean exist(String featId) {
        return featuresMap.containsKey(featId);
    }

    /** {@inheritDoc} */
    @Override
    public void enable(String featID) {
        if (!exist(featID)) {
            throw new FeatureNotFoundException(featID);
        }
        featuresMap.get(featID).enable();
    }

    /** {@inheritDoc} */
    @Override
    public void disable(String featID) {
        if (!exist(featID)) {
            throw new FeatureNotFoundException(featID);
        }
        featuresMap.get(featID).disable();
    }

    /** {@inheritDoc} */
    @Override
    public Feature read(String featureUid) {
        if (!featuresMap.containsKey(featureUid)) {
            throw new FeatureNotFoundException(featureUid);
        }
        return featuresMap.get(featureUid);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Feature> readAll() {
        return featuresMap;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InMemoryFeatureStore [featuresMap=" + featuresMap + "]";
    }

    /**
     * Setter accessor for attribute 'locations'.
     * 
     * @param locations
     *            new value for 'locations '
     */
    public void setLocation(String locations) {
        loadConfFile(locations);
    }

}
