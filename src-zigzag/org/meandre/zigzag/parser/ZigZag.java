/* Generated By:JavaCC: Do not edit this line. ZigZag.java */
package org.meandre.zigzag.parser;

import java.io.*;
import java.util.*;
import org.meandre.zigzag.semantic.*;

public class ZigZag implements ZigZagConstants {

  public static String ZIGZAG_VERSION = "1.0.0vcli";

  protected String sFileName;

  protected FlowGenerator fg;

  public static void main(String args[]) throws ParseException,FileNotFoundException, IOException {
    if ( args.length<1 ) {
        System.err.println("Wrong syntax!!!\nThe compiler requires at least one .zz file");
    }
    else
    {
        for ( String sFileName:args) {
                    FileInputStream fis = new FileInputStream(sFileName);
                    ZigZag parser = new ZigZag(fis);
                    parser.sFileName = sFileName;
                    parser.fg = new FlowGenerator();
                    try {
                        parser.start();
                        System.out.println();
                        parser.fg.generateMAU(sFileName);
                    }
                    catch ( ParseException pe ) {
                        throw pe;
                    }
        }
    }
  }

  final public void start() throws ParseException {
        Token t;
          fg.init(sFileName);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AT:
      case IMPORT:
      case FROM:
      case ALIAS:
      case SYMBOL:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IMPORT:
      case FROM:
      case ALIAS:
        cda();
        break;
      case SYMBOL:
        t = jj_consume_token(SYMBOL);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMA:
        case EQUAL:
          ci(t);
          break;
        case DOT:
          cm(t);
          break;
        case LP:
          ii_call(t);
          break;
        default:
          jj_la1[1] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      case AT:
        ii_assigment();
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(0);
  }

  final public void cda() throws ParseException {
        Token tRepURI;
        Token tCompURI;
        Token tAlias;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IMPORT:
      jj_consume_token(IMPORT);
      tRepURI = jj_consume_token(URI);
                  fg.importRepository(tRepURI.image,jj_input_stream.getBeginLine()-1);
      break;
    case ALIAS:
      jj_consume_token(ALIAS);
      tCompURI = jj_consume_token(URI);
      jj_consume_token(AS);
      tAlias = jj_consume_token(SYMBOL);
                  fg.aliasCoponent(tCompURI.image,tAlias.image,jj_input_stream.getBeginLine()-1);
      break;
    case FROM:
      jj_consume_token(FROM);
      tRepURI = jj_consume_token(URI);
      jj_consume_token(IMPORT);
      tCompURI = jj_consume_token(URI);
                  fg.importRepository(tRepURI.image,tCompURI.image,jj_input_stream.getBeginLine()-1);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AS:
        jj_consume_token(AS);
        tAlias = jj_consume_token(SYMBOL);
                          fg.aliasCoponent(tCompURI.image,tAlias.image,jj_input_stream.getBeginLine()-1);
        break;
      default:
        jj_la1[3] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void ci(Token t) throws ParseException {
        Token tTmp;
        Queue<String> qSymbols = new LinkedList<String>();
        Queue<String> qComponents = new LinkedList<String>();
        qSymbols.offer(t.image);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_2;
      }
      jj_consume_token(COMA);
      tTmp = jj_consume_token(SYMBOL);
                  qSymbols.offer(tTmp.image);
    }
    jj_consume_token(EQUAL);
    tTmp = jj_consume_token(SYMBOL);
    jj_consume_token(LP);
    jj_consume_token(RP);
          qComponents.offer(tTmp.image);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_3;
      }
      jj_consume_token(COMA);
      tTmp = jj_consume_token(SYMBOL);
      jj_consume_token(LP);
      jj_consume_token(RP);
                  qComponents.offer(tTmp.image);
    }
                if ( qSymbols.size()!=qComponents.size() )
                        {if (true) throw new ParseException (
                                "Wrong number of elements in assigment, "+
                                qSymbols.size()+" on the left side and "+
                                qComponents.size()+" on the right side on line "+
                                (jj_input_stream.getBeginLine()-1)
                        );}
                fg.instantiateComponents(qSymbols,qComponents,jj_input_stream.getBeginLine()-1);
  }

  final public void cm(Token t) throws ParseException {
        Token tIns;
        Token tProp;
        Queue<String> qLeftIns = new LinkedList<String>();
        Queue<String> qLeftProp = new LinkedList<String>();
        Queue<String> qRightIns = new LinkedList<String>();
        Queue<String> qRightProp = new LinkedList<String>();

        qLeftIns.offer(t.image);
    jj_consume_token(DOT);
    tProp = jj_consume_token(SYMBOL);
          qLeftProp.offer(tProp.image);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_4;
      }
      jj_consume_token(COMA);
      tIns = jj_consume_token(SYMBOL);
      jj_consume_token(DOT);
      tProp = jj_consume_token(SYMBOL);
                  qLeftIns.offer(tIns.image); qLeftProp.offer(tProp.image);
    }
    jj_consume_token(EQUAL);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VALUE:
      tIns = jj_consume_token(VALUE);
                  qRightIns.offer(tIns.image); qRightProp.offer(null);
      break;
    case SYMBOL:
      tIns = jj_consume_token(SYMBOL);
      jj_consume_token(DOT);
      tProp = jj_consume_token(SYMBOL);
                  qRightIns.offer(tIns.image); qRightProp.offer(tProp.image);
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[9] = jj_gen;
        break label_5;
      }
      jj_consume_token(COMA);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VALUE:
        tIns = jj_consume_token(VALUE);
                          qRightIns.offer(tIns.image); qRightProp.offer(null);
        break;
      case SYMBOL:
        tIns = jj_consume_token(SYMBOL);
        jj_consume_token(DOT);
        tProp = jj_consume_token(SYMBOL);
                         qRightIns.offer(tIns.image); qRightProp.offer(tProp.image);
        break;
      default:
        jj_la1[10] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
                if ( qLeftIns.size()!=qRightIns.size() )
                        {if (true) throw new ParseException (
                                "Wrong number of elements in component modification assigment, "+
                                qLeftIns.size()+" on the left side and "+
                                qRightIns.size()+" on the right side on line "+
                                (jj_input_stream.getBeginLine()-1)
                        );}

                fg.setProperties(qLeftIns,qLeftProp,qRightIns,qRightProp,jj_input_stream.getBeginLine()-1);
  }

  final public void ii_call(Token tCall) throws ParseException {
    jj_consume_token(LP);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SYMBOL:
        ;
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_6;
      }
      port_binding(tCall);
      label_7:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case SEMICOLON:
          ;
          break;
        default:
          jj_la1[12] = jj_gen;
          break label_7;
        }
        jj_consume_token(SEMICOLON);
        port_binding(tCall);
      }
    }
    jj_consume_token(RP);
  }

  final public void port_binding(Token tTargetIns) throws ParseException {
        Token tTargetPort;
        Token tSourceIns;
        Token tSourcePort;
    tTargetPort = jj_consume_token(SYMBOL);
    jj_consume_token(COLON);
    tSourceIns = jj_consume_token(SYMBOL);
    jj_consume_token(DOT);
    tSourcePort = jj_consume_token(SYMBOL);
          fg.bindPort(tSourceIns.image,tSourcePort.image,tTargetIns.image,tTargetPort.image,jj_input_stream.getBeginLine()-1);
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[13] = jj_gen;
        break label_8;
      }
      jj_consume_token(COMA);
      tSourceIns = jj_consume_token(SYMBOL);
      jj_consume_token(DOT);
      tSourcePort = jj_consume_token(SYMBOL);
                  fg.bindPort(tSourceIns.image,tSourcePort.image,tTargetIns.image,tTargetPort.image,jj_input_stream.getBeginLine()-1);
    }
  }

  final public void ii_assigment() throws ParseException {
        Token tBinding;
        Token tTargetIns;
        Queue<String> qLeftIns = new LinkedList<String>();

        int iLeftCount = 1;
        int iRightCount = 1;
    jj_consume_token(AT);
    tBinding = jj_consume_token(SYMBOL);
          fg.createBindingPort(tBinding.image,jj_input_stream.getBeginLine()-1); qLeftIns.offer(tBinding.image);
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[14] = jj_gen;
        break label_9;
      }
      jj_consume_token(COMA);
      jj_consume_token(AT);
      tBinding = jj_consume_token(SYMBOL);
                  fg.createBindingPort(tBinding.image,jj_input_stream.getBeginLine()-1); qLeftIns.offer(tBinding.image); iLeftCount++;
    }
    jj_consume_token(EQUAL);
    tTargetIns = jj_consume_token(SYMBOL);
          fg.bindBindingPort(qLeftIns.poll(),tTargetIns.image,jj_input_stream.getBeginLine()-1);
    ii_call(tTargetIns);
    label_10:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMA:
        ;
        break;
      default:
        jj_la1[15] = jj_gen;
        break label_10;
      }
      jj_consume_token(COMA);
      tTargetIns = jj_consume_token(SYMBOL);
                  fg.bindBindingPort(qLeftIns.poll(),tTargetIns.image,jj_input_stream.getBeginLine()-1);
      ii_call(tTargetIns);
                  iRightCount++;
    }
                if ( iLeftCount!=iRightCount )
                        {if (true) throw new ParseException (
                                "Wrong number of elements in component binding assigment, "+
                                iLeftCount+" on the left side and "+
                                iRightCount+" on the right side on line "+
                                (jj_input_stream.getBeginLine()-1)
                        );}
  }

  public ZigZagTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[16];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0xbc000,0xb80,0xbc000,0x40000,0x38000,0x80,0x80,0x80,0x280000,0x80,0x280000,0x80000,0x2000,0x80,0x80,0x80,};
   }

  public ZigZag(java.io.InputStream stream) {
     this(stream, null);
  }
  public ZigZag(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ZigZagTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public ZigZag(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ZigZagTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public ZigZag(ZigZagTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(ZigZagTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[22];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 16; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 22; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
