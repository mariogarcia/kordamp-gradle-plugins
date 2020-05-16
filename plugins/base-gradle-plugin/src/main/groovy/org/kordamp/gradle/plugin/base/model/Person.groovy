/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Person {
    String id
    String name
    String email
    String url
    String timezone
    Organization organization
    List<String> roles = []
    Map<String, String> properties = [:]

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            id          : id,
            name        : name,
            email       : email,
            url         : url,
            timezone    : timezone,
            organization: organization?.toMap(),
            roles       : roles,
            properties  : properties
        ])
    }

    Person copyOf() {
        Person copy = new Person()
        copy.id = id
        copy.name = name
        copy.email = email
        copy.url = url
        copy.timezone = timezone
        copy.organization = organization?.copyOf()
        List<String> rls = new ArrayList<>(copy.roles)
        copy.roles.clear()
        copy.roles.addAll(rls + roles)
        copy.properties.putAll(properties)
        copy
    }

    static Person merge(Person o1, Person o2) {
        o1.id = o1.id ?: o2?.id
        o1.name = o1.name ?: o2?.name
        o1.email = o1.email ?: o2?.email
        o1.url = o1.url ?: o2?.url
        o1.timezone = o1.timezone ?: o2?.timezone
        CollectionUtils.merge(o1.roles, o2?.roles)
        if (o1.organization) {
            Organization.merge(o1.organization, o2?.organization)
        } else {
            o1.organization = o2?.organization?.copyOf()
        }
        CollectionUtils.merge(o1.properties, o2?.properties)

        o1
    }

    void organization(Action<? super Organization> action) {
        if (!organization) organization = new Organization()
        action.execute(organization)
    }

    void organization(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Organization) Closure<Void> action) {
        if (!organization) organization = new Organization()
        ConfigureUtil.configure(action, organization)
    }
}
