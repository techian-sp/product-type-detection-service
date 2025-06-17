<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd">

    <changeSet id="create-products-table" author="lilyai">
        <createTable tableName="products">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="style_id" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="sku_id" type="VARCHAR(255)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="title" type="VARCHAR(500)">
                <constraints nullable="false"/>
            </column>
            <column name="description" type="TEXT">
                <constraints nullable="true"/>
            </column>
            <column name="brand" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
        
        <createIndex indexName="idx_products_style_id" tableName="products">
            <column name="style_id"/>
        </createIndex>
        
        <createIndex indexName="idx_products_sku_id" tableName="products">
            <column name="sku_id"/>
        </createIndex>
        
        <createIndex indexName="idx_products_brand" tableName="products">
            <column name="brand"/>
        </createIndex>
        
        <createIndex indexName="idx_products_created_at" tableName="products">
            <column name="created_at"/>
        </createIndex>
    </changeSet>
    
    <changeSet id="add-update-trigger-products" author="lilyai" dbms="postgresql">
        <sql>
            CREATE OR REPLACE FUNCTION update_updated_at_column()
            RETURNS TRIGGER AS $$
            BEGIN
                NEW.updated_at = CURRENT_TIMESTAMP;
                RETURN NEW;
            END;
            $$ language 'plpgsql';
            
            CREATE TRIGGER update_products_updated_at
                BEFORE UPDATE ON products
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column();
        </sql>
    </changeSet>
    
    <changeSet id="add-update-trigger-products-mysql" author="lilyai" dbms="mysql">
        <sql>
            CREATE TRIGGER update_products_updated_at
                BEFORE UPDATE ON products
                FOR EACH ROW
                SET NEW.updated_at = CURRENT_TIMESTAMP;
        </sql>
    </changeSet>

</databaseChangeLog>