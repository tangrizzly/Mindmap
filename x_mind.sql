/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50720
 Source Host           : localhost:3306
 Source Schema         : x_mind

 Target Server Type    : MySQL
 Target Server Version : 50720
 File Encoding         : 65001

 Date: 09/06/2018 23:54:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for document
-- ----------------------------
DROP TABLE IF EXISTS `Document`;
CREATE TABLE `Document`  (
  `documentId` INTEGER NOT NULL AUTO_INCREMENT,
  `documentName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `createAt` date NULL DEFAULT NULL,
  `updateAt` timestamp,
  PRIMARY KEY (`documentId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for Version
-- ----------------------------
DROP TABLE IF EXISTS `Version`;
CREATE TABLE `Version`  (
  `versionId` INTEGER NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updateAt` timestamp,
  `userId` INTEGER,
  `versionNote` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `documentId` INTEGER,
  PRIMARY KEY (`versionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for Editor
-- ----------------------------
DROP TABLE IF EXISTS `Editor`;
CREATE TABLE `Editor`  (
  `editId` INTEGER NOT NULL AUTO_INCREMENT,
  `userId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `versionId` INTEGER NULL DEFAULT NULL,
  PRIMARY KEY (`editId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `User`;
CREATE TABLE User (
    userId integer PRIMARY KEY AUTO_INCREMENT,
    userName varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci,
    userPassword varchar(25),
    userGender boolean,
    headPortrait varchar(100),
    company varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci,
    email varchar(50),
    creed varchar(400) CHARACTER SET utf8 COLLATE utf8_general_ci,
    createAt date,
    updateAt timestamp
);

CREATE TABLE Token (
    userId integer PRIMARY KEY,
    token varchar(400)
);

SET FOREIGN_KEY_CHECKS = 1;
