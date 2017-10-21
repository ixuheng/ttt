package com.jk.dao;

import java.util.List;
import java.util.Map;

import com.jk.model.MlsGoods;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MlsGoodsMapper {

    int deleteByPrimaryKey(Integer id) throws Exception;
    @Insert("insert into mls_goods (id, goodName, goodInfo, \n" +
            "      goodPrice, goodImgUrl, goodSave, \n" +
            "      goodState)\n" +
            "    values (#{id,jdbcType=INTEGER}, #{goodname,jdbcType=VARCHAR}, #{goodinfo,jdbcType=VARCHAR}, \n" +
            "      #{goodprice,jdbcType=REAL}, #{goodimgurl,jdbcType=VARCHAR}, #{goodsave,jdbcType=INTEGER}, \n" +
            "      #{goodstate,jdbcType=INTEGER})")
    int insert(MlsGoods record) throws Exception;
    @Select("select count(*)\n" +
            "    from mls_goods")
    int selectAllCount() throws Exception;
    @Insert("insert into mls_goods (id, goodName, goodInfo, \n" +
            "      goodPrice, goodImgUrl, goodSave, \n" +
            "      goodState)\n" +
            "    values (#{id,jdbcType=INTEGER}, #{goodname,jdbcType=VARCHAR}, #{goodinfo,jdbcType=VARCHAR}, \n" +
            "      #{goodprice,jdbcType=REAL}, #{goodimgurl,jdbcType=VARCHAR}, #{goodsave,jdbcType=INTEGER}, \n" +
            "      #{goodstate,jdbcType=INTEGER})")
    int insertSelective(MlsGoods record) throws Exception;

    <T> void batchAddCompany(List<T> list) throws Exception;
    @Select("select * \n" +
            "    from mls_goods\n" +
            "    where id = #{id}")
    MlsGoods selectByPrimaryKey(MlsGoods record) throws Exception;
    @Select("select * from mls_goods")
    List<MlsGoods> selectByPrimaryAll() throws Exception;
    @Select("select * from mls_goods limit #{start},#{page}")
    List<MlsGoods> selectPage(Map<String, Object> map) throws Exception;
    
    int updateByPrimaryKeySelective(MlsGoods record) throws Exception;

    int updateByPrimaryKey(MlsGoods record) throws Exception;
}