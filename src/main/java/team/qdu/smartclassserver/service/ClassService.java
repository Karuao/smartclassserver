package team.qdu.smartclassserver.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.qdu.smartclassserver.dao.ClassMapper;
import team.qdu.smartclassserver.dao.ClassUserMapper;
import team.qdu.smartclassserver.dao.UserMapper;
import team.qdu.smartclassserver.domain.ApiResponse;
import team.qdu.smartclassserver.domain.Class;
import team.qdu.smartclassserver.domain.ClassUser;
import team.qdu.smartclassserver.domain.User;

import java.util.Date;
import java.util.List;

@Service
public class ClassService  {

    @Autowired
    ClassMapper classMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ClassUserMapper classUserMapper;

    //获取用户班课列表
    public String getJoinedClasses(Integer userId) {
        ApiResponse<List<Class>> apiResponse = new ApiResponse<>("0", "success");
        apiResponse.objList = classMapper.selectJoinedClassesByUserId(userId);
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    //进入班课判断用户是老师还是学生
    public String judgeTitle(int classId, int userId) {
        ApiResponse<Void> apiResponse;
        Class clickedClass = classMapper.selectByPrimaryKey(classId);
        if (userId == clickedClass.getUser_id()) {
            apiResponse = new ApiResponse<>("0", "teacher");
        } else {
            apiResponse = new ApiResponse<>("0", "student");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    //不允许加入班课
    public String notAllowToJoin(int classId){
        ApiResponse apiResponse;
        Class cls=classMapper.selectByPrimaryKey(classId);
        cls.setIf_allow_to_join("否");
        int result=classMapper.updateByPrimaryKey(cls);
        if(result==1){
            apiResponse = new ApiResponse("0", "已设置为不允许加入");
        }else{
            apiResponse = new ApiResponse("1", "设置失败");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    public String allowToJoin(int classId){
        ApiResponse apiResponse;
        Class cls=classMapper.selectByPrimaryKey(classId);
        cls.setIf_allow_to_join("是");
        int result=classMapper.updateByPrimaryKey(cls);
        if(result==1){
            apiResponse = new ApiResponse("0", "已设置为允许加入");
        }else{
            apiResponse = new ApiResponse("1", "设置失败");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    public String getClassInfor(int classId){
        Class cls = classMapper.selectByPrimaryKey(classId);
        ApiResponse<Class> apiResponse;
        if(cls!=null){
            //该课程存在
            apiResponse = new ApiResponse<>("0","课程存在");
            apiResponse.obj = cls;
        }else{
            //该课程不存在
            apiResponse = new ApiResponse<>("2", "课程不存在");
        }
        String jsonResponse = new Gson().toJson(apiResponse);

        return jsonResponse;
    }

    public String finishClass(int classId){
        ApiResponse apiResponse;
        Class cls=classMapper.selectByPrimaryKey(classId);
        cls.setIf_allow_to_join("已结束");
        int result=classMapper.updateByPrimaryKey(cls);
        if(result==1){
            apiResponse = new ApiResponse("0", "此班课已结束");
        }else{
            apiResponse = new ApiResponse("1", "失败");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    public String deleteClass(int classId){
        ApiResponse apiResponse;
        Class cls=classMapper.selectByPrimaryKey(classId);
        cls.setIf_allow_to_join("已删除");
        int result=classMapper.updateByPrimaryKey(cls);
        if(result==1){
            apiResponse = new ApiResponse("0", "此班课已删除");
        }else{
            apiResponse = new ApiResponse("1", "删除失败");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    //创建班课
    public String createClass(String name, String course, int userId, String avatarPath) {
        ApiResponse<String> apiResponse;
        User user = userMapper.selectByPrimaryKey(userId);
        //向class表插入班课信息
        Class createdclass = new Class();
        Date date = new Date();
        createdclass.setName(name);
        createdclass.setCourse(course);
        createdclass.setUser_id(userId);
        createdclass.setAvatar(avatarPath);
        createdclass.setUniversity(user.getUniversity());
        createdclass.setDepartment(user.getDepartment());
        createdclass.setPopulation((short) 0);
        createdclass.setIf_allow_to_join("是");
        createdclass.setCreate_date_time(date);
        createdclass.setModify_date_time(date);
        classMapper.insert(createdclass);

        //向class_user表插入老师信息
        ClassUser classUser = new ClassUser();
        classUser.setClass_id(createdclass.getClass_id());
        classUser.setUser_id(userId);
        classUser.setTitle("老师");
        classUser.setIf_in_class("是");
        classUser.setUnread_information_num(0);
        classUser.setExp(0);
        classUser.setCreate_date_time(date);
        classUser.setModify_date_time(date);
        classUserMapper.insert(classUser);

        apiResponse = new ApiResponse<>("0", "创建班课成功");
        apiResponse.obj = createdclass.getClass_id().toString();
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    //修改班课信息
    public String modifyClass(int classId,String avatarPath,String className,String  course,String university,String department,String goal,String exam){
        ApiResponse apiResponse;
        Class cls=classMapper.selectByPrimaryKey(classId);
        cls.setName(className);
        cls.setCourse(course);
        cls.setUniversity(university);
        cls.setDepartment(department);
        cls.setDetail(goal);
        cls.setExam_shedule(exam);
        cls.setAvatar(avatarPath);
        Date date=new Date();
        cls.setModify_date_time(date);
        int result=classMapper.updateByPrimaryKey(cls);
        if(result==1){
            apiResponse = new ApiResponse("0", "修改班课信息成功");
        }else{
            apiResponse = new ApiResponse("1", "修改班课信息失败");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    //加入班课
    public String joinClass(int classId, int userId) {
        ApiResponse apiResponse;
        Class joinedClass = classMapper.selectJoinClassByClassId(classId);
        ClassUser classUser = new ClassUser();
        classUser.setClass_id(classId);
        classUser.setUser_id(userId);
        if (joinedClass == null) {
            //班课不存在
            apiResponse = new ApiResponse("1", "班课不存在");
        } else if (joinedClass.getIf_allow_to_join().equals("已结束")) {
            //班课已结束
            apiResponse = new ApiResponse("2", "班课已结束");
        } else if (joinedClass.getIf_allow_to_join().equals("已删除")) {
            //班课已删除
            apiResponse = new ApiResponse("3", "班课已删除");
        } else if (joinedClass.getIf_allow_to_join().equals("否")) {
            //班课不允许加入
            apiResponse = new ApiResponse("4", "班课不允许加入");
        } else if (classUserMapper.selectByClassIdUserId(classUser) != null) {
            //用户已加入班课
            apiResponse = new ApiResponse("5", "您已加入该班课");
        } else {
            apiResponse = new ApiResponse("0", "查找班课成功");
            apiResponse.setObj(joinedClass);
        }

        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }

    public String confirmjoinClass(int classId, int userId) {
        ApiResponse apiResponse;
        //生成要插入到ClassUser表的记录
        ClassUser classUser = new ClassUser();
        Date date = new Date();
        classUser.setClass_id(classId);
        classUser.setUser_id(userId);
        classUser.setTitle("学生");
        classUser.setIf_in_class("是");
        classUser.setUnread_information_num(0);
        classUser.setExp(0);
        classUser.setCreate_date_time(date);
        classUser.setModify_date_time(date);
        int result = classUserMapper.insert(classUser);

        if (result == 1) {
            apiResponse = new ApiResponse("0", "加入班课成功");
        } else {
            apiResponse = new ApiResponse("1", "加入班课失败，请稍后再试");
        }
        String jsonResponse = new Gson().toJson(apiResponse);
        return jsonResponse;
    }
}
