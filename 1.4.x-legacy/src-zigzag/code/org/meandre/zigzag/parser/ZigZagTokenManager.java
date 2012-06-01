/* Generated By:JavaCC: Do not edit this line. ZigZagTokenManager.java */
package org.meandre.zigzag.parser;
import java.io.*;
import java.util.*;
import org.meandre.zigzag.semantic.*;
import java.util.logging.*;
import org.meandre.core.logger.KernelLoggerFactory;

/** Token Manager. */
public class ZigZagTokenManager implements ZigZagConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0xf80000L) != 0L)
         {
            jjmatchedKind = 25;
            return 19;
         }
         return -1;
      case 1:
         if ((active0 & 0x400000L) != 0L)
            return 19;
         if ((active0 & 0xb80000L) != 0L)
         {
            jjmatchedKind = 25;
            jjmatchedPos = 1;
            return 19;
         }
         return -1;
      case 2:
         if ((active0 & 0xb80000L) != 0L)
         {
            jjmatchedKind = 25;
            jjmatchedPos = 2;
            return 19;
         }
         return -1;
      case 3:
         if ((active0 & 0x900000L) != 0L)
            return 19;
         if ((active0 & 0x280000L) != 0L)
         {
            jjmatchedKind = 25;
            jjmatchedPos = 3;
            return 19;
         }
         return -1;
      case 4:
         if ((active0 & 0x200000L) != 0L)
            return 19;
         if ((active0 & 0x80000L) != 0L)
         {
            jjmatchedKind = 25;
            jjmatchedPos = 4;
            return 19;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         return jjStopAtPos(0, 18);
      case 40:
         return jjStopAtPos(0, 9);
      case 41:
         return jjStopAtPos(0, 10);
      case 43:
         return jjStopAtPos(0, 17);
      case 44:
         return jjStopAtPos(0, 7);
      case 46:
         return jjStopAtPos(0, 11);
      case 58:
         return jjStopAtPos(0, 12);
      case 59:
         return jjStopAtPos(0, 13);
      case 61:
         return jjStopAtPos(0, 8);
      case 64:
         return jjStopAtPos(0, 14);
      case 65:
         return jjMoveStringLiteralDfa1_0(0x800000L);
      case 91:
         return jjStopAtPos(0, 15);
      case 93:
         return jjStopAtPos(0, 16);
      case 97:
         return jjMoveStringLiteralDfa1_0(0x600000L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x80000L);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 85:
         return jjMoveStringLiteralDfa2_0(active0, 0x800000L);
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000L);
      case 109:
         return jjMoveStringLiteralDfa2_0(active0, 0x80000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x100000L);
      case 115:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(1, 22, 19);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 84:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x200000L);
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x80000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 79:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(3, 23, 19);
         break;
      case 97:
         return jjMoveStringLiteralDfa4_0(active0, 0x200000L);
      case 109:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(3, 20, 19);
         break;
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0x80000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 114:
         return jjMoveStringLiteralDfa5_0(active0, 0x80000L);
      case 115:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(4, 21, 19);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 116:
         if ((active0 & 0x80000L) != 0L)
            return jjStartNfaWithStates_0(5, 19, 19);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 19;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3fe000000000000L & l) != 0L)
                  {
                     if (kind > 24)
                        kind = 24;
                     jjCheckNAdd(4);
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  else if (curChar == 60)
                     jjstateSet[jjnewStateCnt++] = 9;
                  else if (curChar == 35)
                     jjCheckNAddTwoStates(1, 2);
                  break;
               case 19:
               case 7:
                  if ((0x3ff080000000000L & l) == 0L)
                     break;
                  if (kind > 25)
                     kind = 25;
                  jjCheckNAdd(7);
                  break;
               case 1:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddTwoStates(1, 2);
                  break;
               case 2:
                  if ((0x2400L & l) != 0L && kind > 6)
                     kind = 6;
                  break;
               case 3:
                  if ((0x3fe000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAdd(4);
                  break;
               case 4:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAdd(4);
                  break;
               case 8:
                  if (curChar == 60)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 10:
                  if (curChar == 47)
                     jjCheckNAdd(11);
                  break;
               case 11:
                  if ((0x27ffe86100000000L & l) != 0L)
                     jjCheckNAddTwoStates(11, 12);
                  break;
               case 12:
                  if (curChar == 62 && kind > 26)
                     kind = 26;
                  break;
               case 13:
                  if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 14:
                  if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 15:
                  if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  break;
               case 17:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 18:
                  if (curChar == 34 && kind > 27)
                     kind = 27;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 25)
                        kind = 25;
                     jjCheckNAddTwoStates(6, 7);
                  }
                  else if (curChar == 95)
                     jjCheckNAddTwoStates(5, 6);
                  break;
               case 19:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 25)
                        kind = 25;
                     jjCheckNAdd(7);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 25)
                        kind = 25;
                     jjCheckNAddTwoStates(6, 7);
                  }
                  break;
               case 1:
                  jjAddStates(3, 4);
                  break;
               case 5:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(5, 6);
                  break;
               case 6:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 25)
                     kind = 25;
                  jjCheckNAddTwoStates(6, 7);
                  break;
               case 7:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 25)
                     kind = 25;
                  jjCheckNAdd(7);
                  break;
               case 9:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(5, 6);
                  break;
               case 11:
                  if ((0x7fffffe87ffffffL & l) != 0L)
                     jjAddStates(7, 8);
                  break;
               case 16:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 17:
                  jjAddStates(0, 2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(3, 4);
                  break;
               case 17:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(0, 2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 19 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   16, 17, 18, 1, 2, 9, 14, 11, 12, 
};

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, "\54", "\75", "\50", "\51", "\56", 
"\72", "\73", "\100", "\133", "\135", "\53", "\41", "\151\155\160\157\162\164", 
"\146\162\157\155", "\141\154\151\141\163", "\141\163", "\101\125\124\117", null, null, null, null, };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
};
static final long[] jjtoToken = {
   0xfffff81L, 
};
static final long[] jjtoSkip = {
   0x7eL, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[19];
private final int[] jjstateSet = new int[38];
protected char curChar;
/** Constructor. */
public ZigZagTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public ZigZagTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 19; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100003600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
