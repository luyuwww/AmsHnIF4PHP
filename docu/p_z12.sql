/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50715
Source Host           : localhost:3306
Source Database       : hnjnnew

Target Server Type    : MYSQL
Target Server Version : 50715
File Encoding         : 65001

Date: 2017-11-22 15:06:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for p_z12
-- ----------------------------
DROP TABLE IF EXISTS `p_z12`;
CREATE TABLE `p_z12` (
  `F1` varchar(200) DEFAULT NULL,
  `F2` varchar(200) DEFAULT NULL,
  `F3` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of p_z12
-- ----------------------------
INSERT INTO `p_z12` VALUES ('正文', null, '公司发文');
INSERT INTO `p_z12` VALUES ('草稿', null, '公司发文');
INSERT INTO `p_z12` VALUES ('办理单', null, '公司发文');
INSERT INTO `p_z12` VALUES ('正文', null, '公司收文');
INSERT INTO `p_z12` VALUES ('草稿', null, '公司收文');
INSERT INTO `p_z12` VALUES ('办理单', null, '公司收文');
INSERT INTO `p_z12` VALUES ('附件', null, '公司收文');
INSERT INTO `p_z12` VALUES ('正文', null, '公司签报');
INSERT INTO `p_z12` VALUES ('草稿', null, '公司签报');
INSERT INTO `p_z12` VALUES ('办理单', null, '公司签报');
INSERT INTO `p_z12` VALUES ('附件', null, '公司签报');
INSERT INTO `p_z12` VALUES ('附件', null, '公司发文');

-- ----------------------------
-- Table structure for p_z5
-- ----------------------------
DROP TABLE IF EXISTS `p_z5`;
CREATE TABLE `p_z5` (
  `F1` varchar(100) NOT NULL,
  `F2` varchar(100) NOT NULL,
  `F3` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of p_z5
-- ----------------------------
INSERT INTO `p_z5` VALUES ('来文单位', 'author', '公司收文');
INSERT INTO `p_z5` VALUES ('备注', 'memo', '公司收文');
INSERT INTO `p_z5` VALUES ('页数', 'pagenum', '公司收文');
INSERT INTO `p_z5` VALUES ('文号', 'docnumber', '公司收文');
INSERT INTO `p_z5` VALUES ('主办部门', 'department', '公司发文');
INSERT INTO `p_z5` VALUES ('商业密级', 'security', '公司发文');
INSERT INTO `p_z5` VALUES ('保密年限', 'securitytime', '公司发文');
INSERT INTO `p_z5` VALUES ('页数', 'pagenum', '公司发文');
INSERT INTO `p_z5` VALUES ('备注', 'memo', '公司发文');
INSERT INTO `p_z5` VALUES ('文号', 'docnumber', '公司发文');
INSERT INTO `p_z5` VALUES ('发文单位', 'author', '公司发文');
INSERT INTO `p_z5` VALUES ('文件日期', 'docdate', '公司发文');
INSERT INTO `p_z5` VALUES ('题名', 'title', '公司发文');
INSERT INTO `p_z5` VALUES ('文件密级', 'security', '公司收文');
INSERT INTO `p_z5` VALUES ('文件日期', 'docdate', '公司收文');
INSERT INTO `p_z5` VALUES ('年度', 'year', '公司收文');
INSERT INTO `p_z5` VALUES ('题名', 'title', '公司收文');
INSERT INTO `p_z5` VALUES ('文号', 'docnumber', '公司签报');
INSERT INTO `p_z5` VALUES ('主办部门', 'department', '公司签报');
INSERT INTO `p_z5` VALUES ('备注', 'memo', '公司签报');
INSERT INTO `p_z5` VALUES ('文件日期', 'docdate', '公司签报');
INSERT INTO `p_z5` VALUES ('题名', 'title', '公司签报');
INSERT INTO `p_z5` VALUES ('年度', 'year', '公司发文');
