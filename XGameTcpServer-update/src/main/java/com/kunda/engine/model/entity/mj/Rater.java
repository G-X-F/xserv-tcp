package com.kunda.engine.model.entity.mj;

public class Rater {
    int id;//评委打分系数id
    int score;//打分


    public Rater(int id , int score){
        this.id = id;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
