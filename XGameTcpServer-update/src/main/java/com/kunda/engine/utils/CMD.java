package com.kunda.engine.utils;


public enum CMD {
    t_null(0),
    h_user_info(1109),
    h_conf_server(1110),
    h_system_mail(1111),

    t_login(4000),
    t_heartbeat(4001),
    t_create_role(4002),
    t_enter_game(4003),
    t_delete_role(4004),
    t_role_skill(4005),
    t_beauty_contest(4006),
    t_create_buddy(4007),
    t_pve_stock(4008),
    t_buddy_piece(4009),
    t_star_level_up(4010),
    t_system_mail(4011),
    t_packet_sys(4012),
    t_fresh_data(4013),
    t_item_sys(4014),

    ;











    private int id;

    CMD(int num){
        this.id = num;
    }

    public int id(){
        return  id;
    }

    public static CMD getCmdById(int id){

        switch (id){
            case 1109:
                return h_user_info;
            case 1110:
                return h_conf_server;
            case 1111:
                return h_system_mail;
            case 4000:
                return t_login;
            case 4001:
                return t_heartbeat;
            case 4002:
                return t_create_role;
            case 4003:
                return t_enter_game;
            case 4004:
                return t_delete_role;
            case 4005:
                return t_role_skill;
            case 4006:
                return t_beauty_contest;
            case 4007:
                return t_create_buddy;
            case 4008:
                return t_pve_stock;
            case 4009:
                return t_buddy_piece;
            case 4010:
                return t_star_level_up;
            case 4011:
                return t_system_mail;
            case 4012:
                return t_packet_sys;
            case 4013:
                return t_fresh_data;
            case 4014:
                return t_item_sys;





            default:
                return t_null;
        }




    }






















}