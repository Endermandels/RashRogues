package io.github.RashRogues;

import java.io.Serializable;

public class ReqCreatePlayer implements Serializable {
   public int nid;
   public String tex;
   public float x;
   public float y;
   public ReqCreatePlayer(int nid, String tex,float x, float y){
      this.nid = nid;
      this.tex = tex;
      this.x = x;
      this.y = y;
   }
}