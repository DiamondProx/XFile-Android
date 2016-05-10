/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.daogenerator.gentest;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

/**
 * Generates entities and DAOs for the example project DaoExample.
 * <p/>
 * Run it as a Java application (not Android).
 *
 * @author Markus
 */
public class ExampleDaoGenerator {

    public static void main(String[] args) throws Exception {

        Schema schema = new Schema(1, "com.huangjiang.dao");
        addDFile(schema);
        addRecordConnect(schema);
        addRecordTransfer(schema);
        new DaoGenerator().generateAll(schema, "D:\\Project\\XFile\\greenDAO-master\\DaoGenerator\\src-gen");
    }


    private static void addDFile(Schema schema) {
        Entity customer = schema.addEntity("DFile");
        customer.addIdProperty();
        customer.addStringProperty("name");
        customer.addStringProperty("taskId");
        customer.addLongProperty("length");
        customer.addLongProperty("position");
        customer.addStringProperty("path");
        customer.addBooleanProperty("isSend");
        customer.addStringProperty("extension");
        customer.addStringProperty("fullName");
        customer.addStringProperty("from");
        customer.addLongProperty("percent");
        customer.addIntProperty("status").notNull();//0创建成功，1传送中（暂停）,2传送完成
        customer.addStringProperty("savePath");// 保存路径
    }

    private static void addRecordConnect(Schema schema) {
        Entity customer = schema.addEntity("DLinkDetail");
        customer.addIdProperty();
        customer.addStringProperty("deviceId");
        customer.addStringProperty("createTime");
    }

    private static void addRecordTransfer(Schema schema) {
        Entity customer = schema.addEntity("DTransferDetail");
        customer.addIdProperty();
        customer.addLongProperty("totalSize");

    }

}
