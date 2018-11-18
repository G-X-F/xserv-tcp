package com.kunda.engine.model.entity.mj;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.cache.WorldMap;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.zip.CRC32;

import static com.kunda.engine.common.fun.Avatas.*;
import static com.kunda.engine.handles.socket.RoleSkillRequestHandler.conf2Skill;
import static com.kunda.engine.model.entity.mj.Buddy.KEY_BUDDY;
import static com.kunda.engine.utils.Const.*;
import static com.kunda.engine.utils.Const.RDSKEY_SIGN;
import static com.kunda.engine.utils.Const.Uidbase;


public class Role {

    private static final Logger logger = LoggerFactory.getLogger(Role.class);


    private long id;//角色id
    private String sign;//角色编号
    private int level;//角色登记
    private long exp;//经验
    private String password;//角色密码
    private String nickname;//昵称
    private String head;//头像地址URL
    private String sex;//1：男  0：女
    private long gold;//金币
    private long stone;//钻石、宝石
    private long coin;//银币
    private int mood;//心情 上限值:100
    private int hp;//体力 上限值:100
    private int charm;//魅力

    private int makings;//气质
    private int shape;//体型
    private int makeup;//妆容
    private int intelligence;//智力
    private int benevolence;//爱心
    private Integer[] skills;//比美技能
    private String del;//删除剩余时间
    private String creatTime;//创建时间



    //构造函数
    public Role(Map<String,String> mp){
        this.id = Long.parseLong(mp.get("id"));
        this.sign = mp.get("sign");
        this.level=Integer.parseInt(mp.get("level"));
        this.exp=Integer.parseInt(mp.get("exp"));
        this.password = mp.get("password");
        this.nickname = mp.get("nickname");
        this.head = mp.get("head");
        this.sex = mp.get("sex");
        this.gold=Long.parseLong(mp.get("gold"));
        this.stone=Integer.parseInt(mp.get("stone"));
        this.coin=Integer.parseInt(mp.get("coin"));
        this.mood=Integer.parseInt(mp.get("mood"));
        this.hp=Integer.parseInt(mp.get("hp"));
        this.charm=Integer.parseInt(mp.get("charm"));
        this.skills=String2Array(mp.get("skills"));

        this.makings =Integer.parseInt(mp.get("makings"));
        this.shape =Integer.parseInt(mp.get("shape"));
        this.makeup =Integer.parseInt(mp.get("makeup"));
        this.intelligence =Integer.parseInt(mp.get("intelligence"));
        this.benevolence =Integer.parseInt(mp.get("benevolence"));
        this.del = mp.get("del");
        this.creatTime = mp.get("creatTime");
    }


    //根据角色id创建角色 返回角色id 创建失败返回0
    public static long createRoleById(long uid,String nickname, String sex ,String head,int wid,String sign)  {
        long rid = 0;
        long base = uid * 1000 + wid; //uid+wid
        for(int i = 1; i< CREATE_ROLE_LIMIT ; i ++){ //查询可以创建的角色id（最多只能创建3个）
            if(!Rdm.instance().exists( KEY_ROLE(base*10 + i) )) {
                rid = base*10 + i;
                break;
            }
        }

        if(rid == 0 ) return rid; //超过创建角色个数

        if(!Rdm.instance().exists( KEY_ROLE(rid) )){//不存在该角色

            //创建Role表
            Map<String,String> rolemp = new HashMap<>();
            rolemp.put("id",String.valueOf(rid));
            rolemp.put("sign",sign);
            rolemp.put("level",String.valueOf(1));//角色初始级别1
            rolemp.put("exp",String.valueOf(0));
            rolemp.put("password",BLANK);
            rolemp.put("nickname",nickname);
            rolemp.put("head",head);
            rolemp.put("sex",sex);//1:男 0:女
            rolemp.put("gold",String.valueOf(0));
            rolemp.put("stone",String.valueOf(0));
            rolemp.put("coin",String.valueOf(0));
            rolemp.put("mood","100");//心情
            rolemp.put("hp","100");//体力
            rolemp.put("charm","10");//魅力
            rolemp.put("skills",BLANK);//比美技能

            rolemp.put("makings","20");//气质
            rolemp.put("shape","50");//体型
            rolemp.put("makeup","5");//妆容
            rolemp.put("intelligence","10");//智力
            rolemp.put("benevolence","10");//爱心
            rolemp.put("del",BLANK);//删除剩余时间

            rolemp.put("creatTime",DateFormatSS.format(System.currentTimeMillis()));

            Rdm.instance().hmset(KEY_ROLE(rid),rolemp);
            //创建昵称映射，昵称在世界范围唯一
            Rdm.instance().hmset(KEY_NICK(nickname,wid),nickname,String.valueOf(uid));

        }

        return rid;
    }



    public PbBodyRole.Role toPbBody(){
            PbBodyRole.Role.Builder role = PbBodyRole.Role.newBuilder();
            role.setSign(this.getSign());
            role.setLevel(this.getLevel());
            role.setExp(this.getExp());
            role.setPassword(this.getPassword());
            role.setNickname(this.getNickname());
            role.setHead(this.getHead());
            role.setSex(Integer.parseInt(this.getSex()));
            role.setGold(this.getGold());
            role.setStone(this.getStone());
            role.setCoin(this.getCoin());
            role.setMood(this.getMood());
            role.setHp(this.getHp());
            role.setMaking(this.getMakings());
            role.setCharm(this.getCharm());
            role.setShape(this.getShape());
            role.setMakeup(this.getMakeup());
            role.setIntelligence(this.getIntelligence());
            role.setBenevolence(this.getBenevolence());
            role.setDel(this.getDel());
            role.setCreatTime(this.getCreatTime());
            return role.build();
    }

    //增加一个技能
    public void addskill(Integer skill){
        this.skills = ArrayAdd(skills,skill);
        Rdm.instance().hmset(KEY_ROLE(this.id),"skills",Array2String(skills));
    }

    //移除一个技能
    public void rmvskill(Integer skill){
        this.skills = ArrayRemove(skills,skill);
        Rdm.instance().hmset(KEY_ROLE(this.id),"skills",Array2String(skills));
    }

    //升级一个技能
    public void upgskill(Integer skill){
        String skl = Array2String(skills).replace(String.valueOf(skill),String.valueOf(skill+1));
        this.skills = String2Array(skl);
        Rdm.instance().hmset(KEY_ROLE(this.id),"skills",skl);
    }



    //生成RoleKey
    private static String KEY_ROLE(long role){
        return RDSKEY_ROLE + ":" + role%(Uidbase/10)/10 +":" + role;
    }

    //生成MyBuddyKey
    private static String KEY_MY_BUDDY(long role ){
        return RDSKEY_MY_BUDDY + ":" + role%(Uidbase/10)/10 +":" + role;
    }

    //生成MyBuddyPieceKey
    private static String KEY_BUDDY_PIECE(long role ){
        return RDSKEY_BUDDY_PIECE + ":" + role%(Uidbase/10)/10 +":" + role;
    }

    //生成BuddyPveStockKey
    private static String KEY_STOCK_PVE(long role ){
        return RDSKEY_STOCK_PVE + ":" + role%(Uidbase/10)/10 +":" + role;
    }

    //生成LastRoleKey
    private static String KEY_LAST_ROLE(long uid ){
        return RDSKEY_LAST_ROLE + ":" + (uid-Uidbase)/1000 +":" + uid;
    }

    //生成NickKey
    private static String KEY_NICK(String nickname,int wid ){
        CRC32 crc32 = new CRC32();//CRC32算法
        crc32.update(nickname.getBytes());
        return RDSKEY_ROLE_NICK + ":" +wid + ":" + crc32.getValue()/Uidbase +":" + nickname ;
    }

    //获取上次登录的角色Id
    public static String lastLoginRole(long uid ){
        if(!Rdm.instance().exists( KEY_LAST_ROLE(uid) )) return BLANK;
        return Rdm.instance().get(KEY_LAST_ROLE(uid));
    }

    //更新上次登录的角色Id
    public static void updateLastRole(long uid,String role ){
         Rdm.instance().set(KEY_LAST_ROLE(uid),role);
    }

    //昵称是否存在
    public static boolean nickExist(String nickname,int wid ){
        return  Rdm.instance().exists(KEY_NICK(nickname,wid));
    }

    //根据rid获得角色信息
    public static Role getRoleById(long role){
        if(Rdm.instance().exists(KEY_ROLE(role))){
            Map<String,String> mp =  Rdm.instance().hgetAll(KEY_ROLE(role));
            return new Role(mp);
        }
        return null;
    }

    /**
     * 根据游戏编号获取角色信息
     * @param sign
     * @return
     */
    public static Role getRoleBySign(String sign){
        if(Rdm.instance().exists(KEY_SIGN(sign))){
            String rid = Rdm.instance().get(KEY_SIGN(sign));
            return getRoleById(Long.parseLong(rid));
        }
        return null;
    }




    //获取本服用户的所有角色信息
    public static List<Role> getAllRole(long uid,int wid ){

        List<Role> list = new ArrayList<>();
        for(int i = 1; i< CREATE_ROLE_LIMIT ; i ++){ //查询可以创建的角色id（最多只能创建3个）
            long rid = (uid * 1000 + wid)*10 + i;
            if(Rdm.instance().exists( KEY_ROLE(rid) ) ) {
                list.add(getRoleById(rid));
            }
        }

        return list;
    }


    //设置删除角色时间
    public void setDel(long milliseconds){
        String date = DateFormatSS.format(new Date(milliseconds));
        this.del = date;
        Rdm.instance().hmset(KEY_ROLE(this.id),"del",date);
        Rdm.instance().setPExpireTime(KEY_ROLE(this.id),milliseconds);
    }

    //计算魅力值
    public long charm() throws InvalidProtocolBufferException {
        //魅力=气质/气质魅力系数+体型/体型魅力系数+妆容/妆容魅力系数+智力/智力魅力系数+爱心/爱心魅力系数

        String charm_record =  ExcelCache.inner().get("觅我游戏版配置表","魅力系数",1);
        String[] charm_rates = charm_record.split(",");

        float mk_rt= Float.parseFloat(charm_rates[1]);
        float sp_rt= Float.parseFloat(charm_rates[2]);
        float mp_rt= Float.parseFloat(charm_rates[3]);
        float in_rt= Float.parseFloat(charm_rates[4]);
        float be_rt= Float.parseFloat(charm_rates[5]);

        float charm = makings/mk_rt + shape/sp_rt + makeup/mp_rt + intelligence/in_rt + benevolence/be_rt;
        BigDecimal decimal = new BigDecimal(charm);
        return  decimal.intValue();
    }

    //评委打分
    public int raterscore(int rater,double buff) throws InvalidProtocolBufferException {
        String charm_record =  ExcelCache.inner().get("觅我游戏版配置表","魅力系数",1);
        String[] charm_rates = charm_record.split(",");

        float mk_rt= Float.parseFloat(charm_rates[1]);
        float sp_rt= Float.parseFloat(charm_rates[2]);
        float mp_rt= Float.parseFloat(charm_rates[3]);
        float in_rt= Float.parseFloat(charm_rates[4]);
        float be_rt= Float.parseFloat(charm_rates[5]);

        String rater_record =  ExcelCache.inner().get("觅我游戏版配置表","评委系数",rater);
        String[] rater_rates = rater_record.split(",");
        float mk_re= Float.parseFloat(rater_rates[1]);
        float sp_re= Float.parseFloat(rater_rates[2]);
        float mp_re= Float.parseFloat(rater_rates[3]);
        float in_re= Float.parseFloat(rater_rates[4]);
        float be_re= Float.parseFloat(rater_rates[5]);
        double base = makings/mk_rt*mk_re + shape/sp_rt*sp_re + makeup/mp_rt*mp_re + intelligence/in_rt*in_re + benevolence/be_rt*be_re;
        base = base * (1 + buff); //基准评分

        double wing_it = mood >= 90 ? 0.99 * 0.99  : (mood * 0.005 + 0.5) * 0.99 * 0.99;//临场发挥系数
        double rand = (new Random().nextInt(20) + 90) * 0.01;//0.90~1.10之间随机
        double score =  base * rand * wing_it;
        BigDecimal decimal = new BigDecimal(score);
        return decimal.intValue();
    }

    //释放概率计算、并返回 该回合 权重最高的一个被动技能
    public PbBodySkill.SkillList passiveSkill(int step) throws InvalidProtocolBufferException {
        PbBodySkill.SkillList.Builder list = PbBodySkill.SkillList.newBuilder();
        PbBodySkill.Skill.Builder passive = null;

        List<String> records =  ExcelCache.inner().mult("觅我游戏版配置表","技能配置表",skills);

        for(int i =0; i< skills.length;i++){
            String record =  records.get(i);
            PbBodySkill.Skill skill = conf2Skill(record);
            if(skill.getType() == 2 && skill.getCastphase() == step){ //如果是被动技能,且在该阶段释放
                //计算释放概率
                if(new Random().nextInt(10000) < skill.getCastrate() ){
                    if(passive == null){
                        passive = skill.toBuilder();
                    }else if(passive.getCastsort() < skill.getCastsort()){ //权重最高
                        passive = skill.toBuilder();
                    }
                }
            }
        }
        if(passive != null){
            list.addSkills(passive);
        }

        return list.build();
    }

    //释放概率计算、并返回 该回合 权重最高的一个主动技能
    public PbBodySkill.SkillList activeSkill(int step) throws InvalidProtocolBufferException {
        PbBodySkill.SkillList.Builder list = PbBodySkill.SkillList.newBuilder();
        PbBodySkill.Skill.Builder active = null;
        List<String> records =  ExcelCache.inner().mult("觅我游戏版配置表","技能配置表",skills);

        for(int i =0; i< skills.length;i++){
            String record =  records.get(i);
            PbBodySkill.Skill skill = conf2Skill(record);
            if(skill.getType() == 1 && skill.getCastphase() == step){ //如果是被动技能,且在该回合释放
                //计算释放概率
                if(new Random().nextInt(10000) < skill.getCastrate()){
                    if(active == null){
                        active = skill.toBuilder();
                    }else if(active.getCastsort() < skill.getCastsort()){ //权重最高
                        active = skill.toBuilder();
                    }
                }
            }
        }
        if(active != null){
            list.addSkills(active);
        }

        return list.build();
    }



    //增加一个伙伴到我的伙伴仓库
    public static void add2MyBuddy(long rid, int bid){
        Rdm.instance().hmset(KEY_MY_BUDDY(rid),String.valueOf(bid),String.valueOf(ZERO)); //field：伙伴ID value:伙伴状态(锁住)
    }

    //从我的仓库删除一个伙伴
    public static void rmvMyBuddy(long rid, int bid){
        Rdm.instance().hdel(KEY_MY_BUDDY(rid),String.valueOf(bid)); //field：伙伴ID
        Rdm.instance().del(KEY_BUDDY(rid,bid));
    }

    //设置pve出战阵容
    public static void upPveStock(long rid, Map<String,String> mp ){
        if(mp.size() > 0){
            Rdm.instance().hmset(KEY_STOCK_PVE(rid),mp); //field：伙伴ID- 阵容位置
        }
    }

    //设置pve出战阵容
    public static Map<String,String> getPveStock(long rid ){
        Map<String,String> map = new HashMap<>();
        if(Rdm.instance().exists(KEY_STOCK_PVE(rid))){
            return  Rdm.instance().hgetAll(KEY_STOCK_PVE(rid)); //field：伙伴ID- 阵容位置
        }
        return map;
    }

    //我的伙伴仓库列表
    public static Map<String, String> myBuddyList(long rid){
        HashMap<String, String> mp = new HashMap<>();
        if(Rdm.instance().exists(KEY_MY_BUDDY(rid))){
            return Rdm.instance().hgetAll(KEY_MY_BUDDY(rid)); //field：伙伴ID
        }
        return mp;
    }


    //增加我的伙伴碎片 cid:细胞id num:数量
    public static int addBuddyPiece(long rid,String cid,int num){
        int cnum = 0;
        if(Rdm.instance().hexist(KEY_BUDDY_PIECE(rid),cid)){
             cnum = Integer.parseInt(Rdm.instance().hGet(KEY_BUDDY_PIECE(rid),cid));
        }
        Rdm.instance().hmset(KEY_BUDDY_PIECE(rid),cid,String.valueOf(cnum + num));
        return cnum;
    }

    //获取我的伙伴碎片数量 cid:细胞id
    public static int getBuddyPiece(long rid,String cid){
        if(Rdm.instance().hexist(KEY_BUDDY_PIECE(rid),cid)){
            return Integer.parseInt(Rdm.instance().hGet(KEY_BUDDY_PIECE(rid),cid));
        }
       return 0;
    }

    //消耗伙伴碎片数量 cid:细胞id
    public static int rmvBuddyPiece(long rid,String cid,int rest,int cost){
        if(Rdm.instance().hexist(KEY_BUDDY_PIECE(rid),cid)){
            Rdm.instance().hmset(KEY_BUDDY_PIECE(rid),cid,String.valueOf(rest-cost));
            return  rest- cost;
        }
        return 0;
    }





    //获取伙伴碎片清单
    public static Map<String,String> getBuddyPieceList(long rid){
        Map<String,String> mp = new HashMap<>();
        if(Rdm.instance().exists(KEY_BUDDY_PIECE(rid)) ){
           return Rdm.instance().hgetAll(KEY_BUDDY_PIECE(rid));
        }
        return mp;
    }



    /**
     * 生产指定长度的字符串，由阿拉伯数字和小写字母组成
     * @param totalLength 字符串总长度
     * @param digitalLength 阿拉伯数字的位数
     * @return
     */
    public static  String createSign(int totalLength,int digitalLength){
        Random random = new Random();
        String result = ""+WorldMap.inner().getCurrentWid();
        for(int i =0;i <(totalLength-digitalLength);i++){
            char ch = (char)(random.nextInt(122 - 97 + 1)+97);
            result += ch;
        }
        for(int j =0;j <digitalLength-1;j++){
            result += random.nextInt(10);
        }
        return result;
    }

    /**
     * 获取不重复的游戏编号
     * @return
     */
    public static String getUnrepeatedSign(int totalLength,int digitalLength){
        boolean a = true;
        String sign = "";
        while(a){
            sign = createSign(totalLength,digitalLength);
            if(Rdm.instance().exists(KEY_SIGN(sign))){
                synchronized (logger){
                    if(Rdm.instance().exists(KEY_SIGN(sign))){
                        setSign(KEY_SIGN(sign),"aa");
                        a = false;
                    }
                }
            }
        }
        return sign;
    }

    /**
     * 根据游戏编号获取用户的角色ID(rid)
     * @param sign 游戏编号
     * @return
     */
    public static String getRid(String sign){

        return Rdm.instance().get(KEY_SIGN(sign));
    }

    /**
     * 存储游戏编号与角色rid的映射
     * @param sign
     * @param rid
     */
    public static void setSign(String sign,String rid){
        Rdm.instance().set(KEY_SIGN(sign),rid);
    }

    /**
     * 生成SIGN key
     * @param sign 游戏编号
     * @return
     */
    public static String KEY_SIGN(String sign){
        return RDSKEY_SIGN+":"+sign;
    }


















    public Integer[] getSkills() {
        return skills;
    }

    public long getId() {
        return id;
    }

    public String getSign(){return sign;}

    public int getLevel() {
        return level;
    }

    public void setLevel(int  level ){
        this.level = level;
        Rdm.instance().hmset(KEY_ROLE(this.id),"level",String.valueOf(level));
    }

    public long getExp() {
        return exp;
    }
    public void setExp(long exp ){
        this.exp = exp;
        Rdm.instance().hmset(KEY_ROLE(this.id),"exp",String.valueOf(exp));
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getHead() {
        return head;
    }

    public String getSex() {
        return sex;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold ){
        this.gold = gold;
        Rdm.instance().hmset(KEY_ROLE(this.id),"gold",String.valueOf(gold));
    }

    public long getStone() {
        return stone;
    }

    public long getCoin() {
        return coin;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood ){
        this.mood = mood;
        Rdm.instance().hmset(KEY_ROLE(this.id),"mood",String.valueOf(mood));
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp ){
        this.hp = hp;
        Rdm.instance().hmset(KEY_ROLE(this.id),"hp",String.valueOf(hp));
    }



    public int getMakings() {
        return makings;
    }

    public int getCharm() {
        return charm;
    }

    public int getShape() {
        return shape;
    }

    public int getMakeup() {
        return makeup;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getBenevolence() {
        return benevolence;
    }

    public String getDel() {
        return del;
    }

    public String getCreatTime() {
        return creatTime;
    }
}
