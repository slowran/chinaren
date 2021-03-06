<%--
  Created by IntelliJ IDEA.
  User: 李浩然
  Date: 2017/7/24
  Time: 13:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!-- Meta, title, CSS, favicons, etc. -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">


    <title><spring:message code="title.app_name" /></title>

    <!-- Bootstrap -->
    <link href="../vendors/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="../vendors/font-awesome/css/font-awesome.min.css" rel="stylesheet">
    <!-- NProgress -->
    <link href="../vendors/nprogress/nprogress.css" rel="stylesheet">
    <!-- iCheck -->
    <link href="../vendors/iCheck/skins/flat/green.css" rel="stylesheet">
    <!-- bootstrap-wysiwyg -->
    <link href="../vendors/google-code-prettify/bin/prettify.min.css" rel="stylesheet">
    <!-- Select2 -->
    <link href="../vendors/select2/dist/css/select2.min.css" rel="stylesheet">
    <!-- Switchery -->
    <link href="../vendors/switchery/dist/switchery.min.css" rel="stylesheet">
    <!-- starrr -->
    <link href="../vendors/starrr/dist/starrr.css" rel="stylesheet">
    <!-- bootstrap-daterangepicker -->
    <link href="../vendors/bootstrap-daterangepicker/daterangepicker.css" rel="stylesheet">

    <!-- Custom Theme Style -->
    <link href="../build/css/custom.min.css" rel="stylesheet">

    <style>
        th{
            text-align: center;
        }
        td{
            text-align: center;
        }
    </style>

</head>

<body class="nav-md">
<div class="container body">
    <div class="main_container">
        <%@ include file="left_top.jspf"%>

        <!-- top navigation -->
        <%@ include file="right_top.jspf"%>
        <!-- /top navigation -->

        <!-- page content -->
        <div class="right_col" role="main">
            <div class="">
                <div class="page-title">
                    <div class="title_left">
                        <h3>我的班级</h3>
                    </div>
                </div>

                <div class="clearfix"></div>

                <!-- my class list -->
                <div class="row">
                    <div class="col-md-12 col-sm-12 col-xs-12">
                        <div class="x_panel">
                            <div class="x_title">
                                <h2>班级列表</h2>
                                <ul class="nav navbar-right panel_toolbox">
                                    <li>
                                        <button class="btn btn-dark" style="margin-right: 5px;"
                                                onclick="table2excel('datatable')">
                                            <i class="fa fa-download"></i>导出Excel
                                        </button>
                                    </li>
                                    <li><a class="collapse-link"><i class="fa fa-chevron-up"></i></a>
                                    </li>
                                </ul>
                                <div class="clearfix"></div>
                            </div>

                            <div class="x_content">
                                <div class="table-responsive">
                                    <table id="datatable" class="table table-striped jambo_table" style="white-space: nowrap;">
                                        <thead>
                                        <tr class="headings">
                                            <th class="column-title">班级ID</th>
                                            <th class="column-title">班级名称</th>
                                            <th class="column-title">班长</th>
                                            <th class="column-title">学校</th>
                                            <th class="column-title">年级</th>
                                            <th class="column-title">地址</th>
                                            <th class="column-title">状态</th>
                                            <th class="column-title">班级介绍</th>
                                            <th class="column-title no-link last"><span class="nobr">操作</span></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${myClasses}" varStatus="status">
                                            <c:choose>
                                                <c:when test="${status.index % 2 == 0}">
                                                    <tr class="even pointer">
                                                </c:when>
                                                <c:otherwise>
                                                    <tr class="odd pointer">
                                                </c:otherwise>
                                            </c:choose>
                                            <td class=" ">${myClasses[status.index].classId}</td>
                                            <td class=" ">${myClasses[status.index].className}</td>
                                            <td class=" ">${myClasses[status.index].managerName}</td>
                                            <td class=" ">${myClasses[status.index].school}</td>
                                            <td class=" ">${myClasses[status.index].gradeYear}</td>
                                            <td class=" ">${myClasses[status.index].province}&nbsp;
                                                    ${myClasses[status.index].city}&nbsp;${myClasses[status.index].area}</td>
                                            <td class=" "><span style="color: #008800;">已加入</span></td>
                                            <td class=" ">
                                                <button type="button" class="btn btn-round btn-info"
                                                        onclick="sAlert('${myClasses[status.index].className}-班级介绍','${myClasses[status.index].description}')">
                                                    查看
                                                </button>
                                            </td>
                                            <td class=" last">
                                                <form action="exitClass" method="post" onsubmit="return checkExitClassForm(this);">
                                                    <input type="hidden" name="user_id" value="${user_id}">
                                                    <input type="hidden" name="manager_id" value="${myClasses[status.index].managerId}">
                                                    <input type="hidden" name="class_id" value="${myClasses[status.index].classId}">
                                                    <input type="submit" value="退出班级"
                                                           onclick="return confirm('确认退出班级-${myClasses[status.index].className}？');"
                                                           class="btn btn-round btn-danger">
                                                </form>
                                            </td>
                                            </tr>
                                        </c:forEach>
                                        <c:forEach items="${applyClasses}" varStatus="status">
                                            <c:choose>
                                                <c:when test="${(status.index + myClasses.size()) % 2 == 0}">
                                                    <tr class="even pointer">
                                                </c:when>
                                                <c:otherwise>
                                                    <tr class="odd pointer">
                                                </c:otherwise>
                                            </c:choose>
                                            <td class=" ">${applyClasses[status.index].classId}</td>
                                            <td class=" ">${applyClasses[status.index].className}</td>
                                            <td class=" ">${applyClasses[status.index].managerName}</td>
                                            <td class=" ">${applyClasses[status.index].school}</td>
                                            <td class=" ">${applyClasses[status.index].gradeYear}</td>
                                            <td class=" ">${applyClasses[status.index].province}&nbsp;
                                                    ${applyClasses[status.index].city}&nbsp;${applyClasses[status.index].area}</td>
                                            <td class=" "><span style="color: #FF1800;">待审核</span></td>
                                            <td class=" ">
                                                <button type="button" class="btn btn-round btn-info"
                                                        onclick="sAlert('${applyClasses[status.index].className}-班级介绍','${applyClasses[status.index].description}')">
                                                    查看
                                                </button>
                                            </td>
                                            <td class=" last">
                                                <form action="exitClass" method="post" onsubmit="return checkExitClassForm(this);">
                                                    <input type="hidden" name="user_id" value="${user_id}">
                                                    <input type="hidden" name="manager_id" value="${applyClasses[status.index].managerId}">
                                                    <input type="hidden" name="class_id" value="${applyClasses[status.index].classId}">
                                                    <input type="submit" value="取消申请"
                                                           onclick="return confirm('确认取消申请？');"
                                                           class="btn btn-round btn-danger">
                                                </form>
                                            </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- /page content -->

        <!-- footer content -->
        <footer>
            <div class="pull-right">
                <spring:message code="text.footer"/>
            </div>
            <div class="clearfix"></div>
        </footer>
        <!-- /footer content -->
    </div>
</div>
<!-- jQuery -->
<script src="../vendors/jquery/dist/jquery.min.js"></script>
<!-- Bootstrap -->
<script src="../vendors/bootstrap/dist/js/bootstrap.min.js"></script>
<!-- FastClick -->
<script src="../vendors/fastclick/lib/fastclick.js"></script>
<!-- NProgress -->
<script src="../vendors/nprogress/nprogress.js"></script>
<!-- iCheck -->
<script src="../vendors/iCheck/icheck.min.js"></script>
<!-- Datatables -->
<script src="../vendors/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="../vendors/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="../vendors/datatables.net-buttons/js/dataTables.buttons.min.js"></script>
<script src="../vendors/datatables.net-buttons-bs/js/buttons.bootstrap.min.js"></script>
<script src="../vendors/datatables.net-buttons/js/buttons.flash.min.js"></script>
<script src="../vendors/datatables.net-buttons/js/buttons.html5.min.js"></script>
<script src="../vendors/datatables.net-buttons/js/buttons.print.min.js"></script>
<script src="../vendors/datatables.net-fixedheader/js/dataTables.fixedHeader.min.js"></script>
<script src="../vendors/datatables.net-keytable/js/dataTables.keyTable.min.js"></script>
<script src="../vendors/datatables.net-responsive/js/dataTables.responsive.min.js"></script>
<script src="../vendors/datatables.net-responsive-bs/js/responsive.bootstrap.js"></script>
<script src="../vendors/datatables.net-scroller/js/dataTables.scroller.min.js"></script>
<script src="../vendors/jszip/dist/jszip.min.js"></script>
<script src="../vendors/pdfmake/build/pdfmake.min.js"></script>
<script src="../vendors/pdfmake/build/vfs_fonts.js"></script>

<!-- Custom Theme Scripts -->
<script src="../build/js/custom.min.js"></script>
<script src="../js/table2excel.js"></script>

<script src="../js/validate.js"></script>
<script src="../js/cusDialog.js"></script>
</body>
</html>
