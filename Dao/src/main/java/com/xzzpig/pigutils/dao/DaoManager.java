package com.xzzpig.pigutils.dao;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.dao.annotation.DBArray;
import com.xzzpig.pigutils.dao.annotation.DBField;
import com.xzzpig.pigutils.dao.annotation.DBForeign;
import com.xzzpig.pigutils.dao.annotation.DBTable;
import com.xzzpig.pigutils.dao.exception.DBArrayTableCreateException;
import com.xzzpig.pigutils.dao.exception.DBForeignObjectCreateException;
import com.xzzpig.pigutils.dao.exception.NotDBTableException;
import com.xzzpig.pigutils.dao.exception.ResultSetSolveFailedException;
import com.xzzpig.pigutils.data.DataUtils;
import com.xzzpig.pigutils.database.DBSelector;
import com.xzzpig.pigutils.database.Database;
import com.xzzpig.pigutils.database.Table;
import com.xzzpig.pigutils.database.TableConstruct;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DaoManager {
    private @NotNull Database database;
    private @NotNull WeakHashMap<Class<?>, TableConstruct> tableConstructWeakHashMap;
    private @NotNull WeakHashMap<Class<?>, Table> tableWeakHashMap;
    private @NotNull List<ResultSetSolver> resultSetSolvers;
    private @NotNull Map<Class<?>, Map<Object, Object>> cacheMap;

    public DaoManager(@NotNull Database database) {
        this.database = database;
        tableConstructWeakHashMap = new WeakHashMap<>();
        tableWeakHashMap = new WeakHashMap<>();
        resultSetSolvers = new LinkedList<>();
        cacheMap = new WeakHashMap<>();
    }

    /**
     * 越后添加的越先执行
     */
    public @NotNull DaoManager addResultSolver(ResultSetSolver resultSetSolver) {
        resultSetSolvers.add(0, resultSetSolver);
        return this;
    }

    public Database getDatabase() {
        return database;
    }

    public DaoManager setDatabase(Database database) {
        this.database = database;
        return this;
    }

    /**
     * @throws NotDBTableException
     */
    public @NotNull TableConstruct solveTableClass(Class<?> clazz) {
        TableConstruct tableConstruct = tableConstructWeakHashMap.get(clazz);
        if (tableConstruct != null)
            return tableConstruct;
        checkIsDBTable(clazz);
        tableConstruct = getDatabase().createConstruct();
        for (Field field : clazz.getDeclaredFields()) {
            DBField dbField = field.getAnnotation(DBField.class);
            if (dbField == null)
                continue;
            String name = dbField.name();
            String defaultValue = dbField.defaultValue();
            String check = dbField.check();
            int size = dbField.size();
            boolean primaryKey;
            boolean unique;
            boolean notNull;
            boolean autoIncrement;
            primaryKey = dbField.primaryKey();
            unique = dbField.unique();
            notNull = dbField.notNull();
            autoIncrement = dbField.autoIncrement();
            JDBCType type = dbField.type();
            if (name.equalsIgnoreCase(""))
                name = field.getName();
            if (defaultValue.equalsIgnoreCase(""))
                defaultValue = null;
            if (check.equals(""))
                check = null;
            com.xzzpig.pigutils.database.DBField dbField1 = tableConstruct.createDBField(name);
            dbField1.classType = field.getType();
            if (field.isAnnotationPresent(DBForeign.class)) {
                Class<?> foreignClass = getForeignClass(field);
                String foreignField = getForeignField(field);
                dbField1.setForeign(getTable(foreignClass).getName(), foreignField);
                dbField1.classType = solveTableClass(foreignClass).findField(foreignField).classType;
                type = solveTableClass(foreignClass).findField(foreignField).getType();
            }
            dbField1.setType(type);
            dbField1.setSize(size);
            dbField1.setDefaultValue(defaultValue);
            dbField1.setPrimaryKey(primaryKey);
            dbField1.setUnique(unique);
            dbField1.setNotNull(notNull);
            dbField1.setCheck(check);
            dbField1.setAutoIncrement(autoIncrement);
            dbField1.DaoFiled = field;
            tableConstruct.addDBField(dbField1);
        }
        tableConstructWeakHashMap.put(clazz, tableConstruct);
        return tableConstruct;
    }

    private Class<?> getForeignClass(Field field) {
        DBForeign foreignKey = field.getAnnotation(DBForeign.class);
        Class<?> foreignClass = foreignKey.table();
        if (foreignClass == Object.class)
            foreignClass = field.getType();
        return foreignClass;
    }

    private String getForeignField(Field field) {
        DBForeign foreignKey = field.getAnnotation(DBForeign.class);
        String foreignField = foreignKey.field();
        if (foreignField.equals(""))
            foreignField = solveTableClass(getForeignClass(field)).findPrimaryKeyField().getName();
        return foreignField;
    }

    private String getArrayTableName(Class<?> clazz, Field field) {
        DBArray dbArray = field.getAnnotation(DBArray.class);
        String name = dbArray.arrayTable();
        if (name.equals(""))
            name = getTable(clazz).getName() + "_" + getTable(field.getType().getComponentType()).getName();
        return name;
    }

    private void createArrayTable(Class<?> clazz, Field field) {
        DBArray dbArray = field.getAnnotation(DBArray.class);
        TableConstruct construct = database.createConstruct();
        com.xzzpig.pigutils.database.DBField dbField_from = solveTableClass(clazz).findPrimaryKeyField();
        com.xzzpig.pigutils.database.DBField dbField_to = solveTableClass(field.getType().getComponentType()).findPrimaryKeyField();
        construct.addDBField(construct.createDBField(dbField_from.getName()).setType(dbField_from.getType()).setNotNull(true).setSize(dbField_from.getSize()).setForeign(getTable(clazz).getName(), dbField_from.getName()));
        construct.addDBField(construct.createDBField(dbField_to.getName()).setType(dbField_to.getType()).setNotNull(true).setSize(dbField_to.getSize()).setForeign(getTable(field.getType().getComponentType()).getName(), dbField_to.getName()));
        try {
            database.createTable(getArrayTableName(clazz, field), construct);
        } catch (SQLException e) {
            throw new DBArrayTableCreateException(e);
        }
    }

    public @NotNull Table createTable(Class<?> clazz) throws SQLException {
        DBTable dbTable = checkIsDBTable(clazz);
        String name = dbTable.name();
        if (name.equalsIgnoreCase(""))
            name = clazz.getName();
        Arrays.stream(clazz.getDeclaredFields()).filter(field->field.isAnnotationPresent(DBArray.class)).forEach(field->createArrayTable(clazz, field));
        return database.createTable(name, solveTableClass(clazz));
    }

    public @NotNull Table getTable(Class<?> clazz) {
        DBTable dbTable = checkIsDBTable(clazz);
        String name = dbTable.name();
        if (name.equalsIgnoreCase(""))
            name = clazz.getName();
        Table table = tableWeakHashMap.get(clazz);
        if (table != null)
            return table;
        table = database.getTable(name).setConstruct(solveTableClass(clazz));
        tableWeakHashMap.put(clazz, table);
        return table;
    }

    public DaoManager insert(Object obj) throws SQLException {
        Class<?> clazz = obj.getClass();
        Map<String, Object> map = getFieldMap(obj);
        getTable(clazz).insert(map);
        if (needCache(clazz))
            this.cache(obj);
        return this;
    }

    private Object getFiledData(Field field, Object obj) {
        boolean access = field.isAccessible();
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(access);
        }
        return null;
    }

    private Map<String, Object> getFieldMap(Object obj) throws SQLException {
        Map<String, Object> map = new Hashtable<>();
        Class<?> clazz = obj.getClass();
        com.xzzpig.pigutils.database.DBField keyField = solveTableClass(obj.getClass()).findPrimaryKeyField();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(DBArray.class)) {
                Object keyValue = getFiledData(keyField.DaoFiled, obj);
                Table arrayTable = database.getTable(getArrayTableName(obj.getClass(), field));
                arrayTable.delete(DataUtils.array2KVMap(String.class, Object.class, keyField.getName(), keyValue));
                DBArray dbArray = field.getAnnotation(DBArray.class);
                Object array = getFiledData(field, obj);
                if (array != null) {
                    for (int i = 0; i < Array.getLength(array); i++) {
                        Object target = Array.get(array, i);
                        if (dbArray.autoIU())
                            insertOrUpdate(target, null, null);
                        Object value = getFiledData(solveTableClass(target.getClass()).findPrimaryKeyField().DaoFiled, target);
                        arrayTable.insert(keyValue, value);
                    }
                }
            }
            DBField dbField = field.getAnnotation(DBField.class);
            if (dbField == null)
                continue;
            String fieldName = dbField.name();
            if (fieldName.equalsIgnoreCase(""))
                fieldName = field.getName();
            boolean access = field.isAccessible();
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            field.setAccessible(access);
            DBForeign dbForeign = field.getAnnotation(DBForeign.class);
            if (dbForeign != null && value != null) {
                String foreignField = getForeignField(field);
                if (dbForeign.autoIU())
                    insertOrUpdate(value, null, null);
                Map<String, Object> foreignMap = getFieldMap(value);
                value = foreignMap.getOrDefault(foreignField, null);
            }
            if (value != null)
                map.put(fieldName, value);
        }
        return map;
    }

    public DaoManager update(Object obj, String where, Object... wherePrepare) throws SQLException {
        Class<?> clazz = obj.getClass();
        Map<String, Object> map = getFieldMap(obj);
        getTable(clazz).update(map, where, wherePrepare);
        if (needCache(clazz))
            this.cache(obj);
        return this;
    }

    public DaoManager insertOrUpdate(@NotNull Object obj, @Nullable String where, @Nullable Object... wherePrepare) throws SQLException {
        if (where == null)
            update(obj);
        else
            update(obj, where, wherePrepare);
        if (getTable(obj.getClass()).getLastUpdateNum() == 0)
            insert(obj);
        return this;
    }

    public DaoManager update(Object obj) throws SQLException {
        Class<?> clazz = obj.getClass();
        TableConstruct construct = solveTableClass(clazz);
        com.xzzpig.pigutils.database.DBField dbField = construct.findPrimaryKeyField();
        Objects.requireNonNull(dbField);
        boolean access = dbField.DaoFiled.isAccessible();
        dbField.DaoFiled.setAccessible(true);
        Object value;
        try {
            value = dbField.DaoFiled.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        dbField.DaoFiled.setAccessible(access);
        update(obj, dbField.getName() + " = ?", value);
        return this;
    }

    public @NotNull Table getOrCreateTable(Class<?> clazz) throws SQLException {
        DBTable dbTable = checkIsDBTable(clazz);
        String name = dbTable.name();
        if (name.equalsIgnoreCase(""))
            name = clazz.getName();
        if (getDatabase().isTableExists(name)) return getTable(clazz);
        else return createTable(clazz);
    }

    /**
     * @throws NotDBTableException
     */
    private @NotNull DBTable checkIsDBTable(Class<?> clazz) {
        DBTable table = clazz.getAnnotation(DBTable.class);
        if (table == null)
            throw new NotDBTableException(clazz.toGenericString() + "has not annotation:DBTable");
        return table;
    }

    private Object createForeignObject(Field field, Object value) throws IllegalAccessException, SQLException, InstantiationException {
        DBForeign foreign = field.getAnnotation(DBForeign.class);

        if (foreign == null) {throw new DBForeignObjectCreateException("field is not annotated by DBForeign");}
        Class<?> foreignClass = getForeignClass(field);
        if (foreign.field().equals("") && cacheMap.containsKey(foreignClass) && cacheMap.get(foreignClass).containsKey(value))
            return cacheMap.get(foreignClass).get(value);
        String foreignField = getForeignField(field);
        TableConstruct tableConstruct = solveTableClass(foreignClass);
        com.xzzpig.pigutils.database.DBField dbField = tableConstruct.findField(foreignField);
        if (dbField == null)
            throw new DBForeignObjectCreateException("no field named " + foreignField + " in the table " + getTable(foreignClass).getName());
        Object obj = null;
        try {
            obj = foreignClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        boolean access = dbField.DaoFiled.isAccessible();
        dbField.DaoFiled.setAccessible(true);
        try {
            dbField.DaoFiled.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            dbField.DaoFiled.setAccessible(access);
        }
        if (foreign.autoFill())
            fill(obj);
        if (needCache(obj.getClass()))
            this.cache(obj);
        return obj;
    }

    private String getFiledName(Field field) {
        DBField dbField = field.getAnnotation(DBField.class);
        String name = dbField.name();
        if (name.equals(""))
            name = field.getName();
        return name;
    }

    public @NotNull <T> List<T> loadByResult(@NotNull Class<T> clazz, @NotNull ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException {
        TableConstruct tableConstruct = solveTableClass(clazz);
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            T result = clazz.newInstance();
            for (com.xzzpig.pigutils.database.DBField dbField : tableConstruct.getDBFields()) {
                Field field = dbField.DaoFiled;
                Object obj = solveToObject(resultSet, dbField);
                if (field.isAnnotationPresent(DBForeign.class))
                    obj = createForeignObject(field, obj);
                boolean access = field.isAccessible();
                field.setAccessible(true);
                field.set(result, obj);
                field.setAccessible(access);
            }
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(DBArray.class))
                    continue;
                boolean access = field.isAccessible();
                field.set(result, getDBArray(clazz, field, result));
                field.setAccessible(access);
            }
            results.add(result);
            if (needCache(clazz))
                this.cache(result);
        }
        resultSet.close();
        return results;
    }

    private Object solveToObject(@NotNull ResultSet resultSet, com.xzzpig.pigutils.database.DBField dbField) throws SQLException {
        Field field = dbField.DaoFiled;
        Object obj = null;
        try {
            obj = resultSet.getObject(dbField.getName());
            if (!field.getType().isInstance(obj))
                obj = resultSet.getObject(dbField.getName(), field.getType());
        } catch (SQLException e) {
            AtomicReference<Object> reference = new AtomicReference<>();
            boolean solved = false;
            for (ResultSetSolver resultSetSolver : resultSetSolvers) {
                if (resultSetSolver.solve(reference, dbField, resultSet)) {
                    obj = reference.get();
                    solved = true;
                    break;
                }
            }
            if (!solved)
                throw new ResultSetSolveFailedException(dbField, e);
        }
        return obj;
    }

    private @NotNull Object getDBArray(Class<?> clazz, Field field, Object obj) throws SQLException, IllegalAccessException, InstantiationException {
        DBArray dbArray = field.getAnnotation(DBArray.class);
        com.xzzpig.pigutils.database.DBField keyField = solveTableClass(clazz).findPrimaryKeyField();
        com.xzzpig.pigutils.database.DBField valueField = solveTableClass(field.getType().getComponentType()).findPrimaryKeyField();
        Object keyValue = getFiledData(keyField.DaoFiled, obj);
        LinkedList<Object> list = new LinkedList<>();
        Table arrayTable = database.getTable(getArrayTableName(clazz, field));
        ResultSet resultSet = arrayTable.select().setWhere(keyField.getName() + " = ?", keyValue).select();
        while (resultSet.next()) {
            list.add(solveToObject(resultSet, valueField));
        }
        Object array = Array.newInstance(field.getType().getComponentType(), list.size());
        boolean access = valueField.DaoFiled.isAccessible();
        for (int i = 0; !list.isEmpty(); i++) {
            Object value = field.getType().getComponentType().newInstance();
            valueField.DaoFiled.set(value, list.pop());
            Array.set(array, i, value);
            if (dbArray.autoFill()) {
                this.fill(value);
            }
            if (checkIsDBTable(value.getClass()).cache())
                this.cache(value);
        }
        valueField.DaoFiled.setAccessible(access);
        return array;
    }

    public @NotNull <T> List<T> select(Class<T> clazz, @NotNull DBSelector selector) throws SQLException, InstantiationException, IllegalAccessException {
        return loadByResult(clazz, selector.select());
    }

    /**
     * 填充Obj
     * 如果baseFields == null或baseFields.length == 0,则取主键
     *
     * @param baseFields 以哪些字段值为基础填充obj
     * @return == obj
     */
    @SuppressWarnings("unchecked")
    public @NotNull <T> T fill(@NotNull T obj, @Nullable String... baseFields) throws SQLException, InstantiationException, IllegalAccessException {
        TableConstruct tableConstruct = solveTableClass(obj.getClass());
        if (baseFields == null || baseFields.length == 0) {
            baseFields = new String[]{tableConstruct.findPrimaryKeyField().getName()};
        }
        List<String> fieldList = Arrays.asList(baseFields);
        Map<com.xzzpig.pigutils.database.DBField, Object> baseMap = new HashMap<>();
        List<com.xzzpig.pigutils.database.DBField> fillList = new ArrayList<>();
        for (com.xzzpig.pigutils.database.DBField field : tableConstruct.getDBFields()) {
            if (fieldList.contains(field.getName())) {
                boolean access = field.DaoFiled.isAccessible();
                field.DaoFiled.setAccessible(true);
                try {
                    baseMap.put(field, field.DaoFiled.get(obj));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } finally {
                    field.DaoFiled.setAccessible(access);
                }
            } else
                fillList.add(field);
        }
        List<Object> whereObj = new ArrayList<>(baseMap.size());
        StringBuilder sb = new StringBuilder();
        baseMap.forEach((field, baseObj)->{
            sb.append(',').append(field.getName()).append('=').append('?');
            whereObj.add(baseObj);
        });
        ResultSet resultSet = getTable(obj.getClass()).select().setWhere(sb.toString().replaceFirst(",", ""), whereObj.toArray()).select();
        List<T> results = loadByResult((Class<T>) obj.getClass(), resultSet);
        if (results.size() > 0) {
            T result = results.get(0);
            for (com.xzzpig.pigutils.database.DBField field : fillList) {
                boolean access = field.DaoFiled.isAccessible();
                field.DaoFiled.setAccessible(true);
                field.DaoFiled.set(obj, field.DaoFiled.get(result));
                field.DaoFiled.setAccessible(access);
            }
        }
        return obj;
    }

    public @NotNull <T> List<T> select(Class<T> clazz, @Nullable Consumer<DBSelector> selectorConsumer) throws SQLException, InstantiationException, IllegalAccessException {
        DBSelector selector = getTable(clazz).select();
        if (selectorConsumer != null)
            selectorConsumer.accept(selector);
        return select(clazz, selector);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T> T select(Class<T> clazz, @NotNull Object keyValue) throws IllegalAccessException, SQLException, InstantiationException {
        if (cacheMap.containsKey(clazz) && cacheMap.get(clazz).containsKey(keyValue))
            return (T) cacheMap.get(clazz).get(keyValue);
        String key = solveTableClass(clazz).findPrimaryKeyField().getName();
        List<T> result = select(clazz, selector->selector.setWhere(key + "= ?", keyValue));
        if (result.size() == 0)
            return null;
        else
            return result.get(0);
    }

    public @NotNull DaoManager cache(Object obj) {
        if (!cacheMap.containsKey(obj.getClass())) cacheMap.put(obj.getClass(), new HashMap<>());
        Map<Object, Object> keyMap = cacheMap.get(obj.getClass());
        TableConstruct tableConstruct = solveTableClass(obj.getClass());
        Field keyField = tableConstruct.findPrimaryKeyField().DaoFiled;
        boolean access = keyField.isAccessible();
        keyField.setAccessible(true);
        try {
            keyMap.put(keyField.get(obj), obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            keyField.setAccessible(access);
        }
        return this;
    }

    public @NotNull DaoManager free(Object obj) {
        if (!cacheMap.containsKey(obj.getClass())) return this;
        Map<Object, Object> keyMap = cacheMap.get(obj.getClass());
        TableConstruct tableConstruct = solveTableClass(obj.getClass());
        Field keyField = tableConstruct.findPrimaryKeyField().DaoFiled;
        boolean access = keyField.isAccessible();
        keyField.setAccessible(true);
        try {
            keyMap.remove(keyField.get(obj));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            keyField.setAccessible(access);
        }
        return this;
    }

    public @NotNull DaoManager free(Class<?> clazz) {
        cacheMap.remove(clazz);
        return this;
    }

    public @NotNull DaoManager free() {
        cacheMap.clear();
        return this;
    }

    private boolean needCache(Class<?> clazz) {
        return checkIsDBTable(clazz).cache();
    }

    public @NotNull DaoManager delete(Object obj) throws SQLException {
        com.xzzpig.pigutils.database.DBField keyField = solveTableClass(obj.getClass()).findPrimaryKeyField();
        boolean access = keyField.DaoFiled.isAccessible();
        keyField.DaoFiled.setAccessible(true);
        Object keyValue;
        try {
            keyValue = keyField.DaoFiled.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        keyField.DaoFiled.setAccessible(access);
        getTable(obj.getClass()).delete(DataUtils.array2KVMap(String.class, Object.class, keyField.getName(), keyValue));
        return this;
    }
}
