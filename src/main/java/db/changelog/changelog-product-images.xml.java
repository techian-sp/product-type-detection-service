<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd">

    <changeSet id="create-product-images-table" author="lilyai">
        <createTable tableName="product_images">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="product_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="image_url" type="VARCHAR(2048)">
                <constraints nullable="false"/>
            </column>
            <column name="type" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="add-product-images-foreign-key" author="lilyai">
        <addForeignKeyConstraint
            baseTableName="product_images"
            baseColumnNames="product_id"
            constraintName="fk_product_images_product_id"
            referencedTableName="products"
            referencedColumnNames="id"
            onDelete="CASCADE"
            onUpdate="RESTRICT"/>
    </changeSet>

    <changeSet id="create-product-images-indexes" author="lilyai">
        <createIndex indexName="idx_product_images_product_id" tableName="product_images">
            <column name="product_id"/>
        </createIndex>
        <createIndex indexName="idx_product_images_type" tableName="product_images">
            <column name="type"/>
        </createIndex>
        <createIndex indexName="idx_product_images_created_at" tableName="product_images">
            <column name="created_at"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>