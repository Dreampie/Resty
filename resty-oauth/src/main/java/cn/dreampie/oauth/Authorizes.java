package cn.dreampie.oauth;

import cn.dreampie.cache.SimpleCache;
import cn.dreampie.common.Constant;
import cn.dreampie.oauth.entity.Code;
import cn.dreampie.oauth.entity.Token;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dreampie on 16/7/7.
 */
public class Authorizes {
  public static final String OAUTH2_CODE_DEF_KEY = "oauth2" + Constant.CONNECTOR + "codes";
  public static final String OAUTH2_TOKEN_DEF_KEY = "oauth2" + Constant.CONNECTOR + "tokens";
  public static final String OAUTH2_REFRESH_TOKEN_DEF_KEY = "oauth2" + Constant.CONNECTOR + "refreshTokens";

  public static final Map<String, String> refreshTokens = new HashMap<String, String>();
  public static final Map<String, Code> codes = new HashMap<String, Code>();
  public static final Map<String, Token> tokens = new HashMap<String, Token>();

  public static void addCode(Code code) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().add(OAUTH2_CODE_DEF_KEY, code.getCode(), code, code.getExpires());
    } else {
      codes.put(code.getCode(), code);
      for (Map.Entry<String, Code> codeEntry : codes.entrySet()) {
        if (codeEntry.getValue().getExpiredAt().compareTo(new Date()) < 0) {
          codes.remove(codeEntry.getKey());
        }
      }
    }
  }

  public static void removeCode(String key) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().remove(OAUTH2_CODE_DEF_KEY, key);
    } else {
      codes.remove(key);
      for (Map.Entry<String, Code> codeEntry : codes.entrySet()) {
        if (codeEntry.getValue().getExpiredAt().compareTo(new Date()) < 0) {
          codes.remove(codeEntry.getKey());
        }
      }
    }
  }

  public static Code getCode(String key) {
    Code code;
    if (Constant.cacheEnabled) {
      code = SimpleCache.instance().get(OAUTH2_CODE_DEF_KEY, key);
    } else {
      code = codes.get(key);
      if (code.getExpiredAt().compareTo(new Date()) < 0) {
        codes.remove(key);
        code = null;
      }
    }
    return code;
  }

  public static void addToken(Token token) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().add(OAUTH2_TOKEN_DEF_KEY, token.getToken(), token, token.getExpires());
    } else {
      tokens.put(token.getToken(), token);
      for (Map.Entry<String, Token> tokenEntry : tokens.entrySet()) {
        if (tokenEntry.getValue().getExpiredAt().compareTo(new Date()) < 0) {
          tokens.remove(tokenEntry.getKey());
          refreshTokens.values().remove(tokenEntry.getKey());
        }
      }
    }
  }

  public static void removeToken(String key) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().remove(OAUTH2_TOKEN_DEF_KEY, key);
    } else {
      tokens.remove(key);
      for (Map.Entry<String, Token> tokenEntry : tokens.entrySet()) {
        if (tokenEntry.getValue().getExpiredAt().compareTo(new Date()) < 0) {
          tokens.remove(tokenEntry.getKey());
          refreshTokens.values().remove(tokenEntry.getKey());
        }
      }
    }
  }

  public static void refreshToken(String refreshToken, String key, Token token) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().remove(OAUTH2_REFRESH_TOKEN_DEF_KEY, refreshToken);
      SimpleCache.instance().remove(OAUTH2_TOKEN_DEF_KEY, key);
    } else {
      refreshTokens.remove(refreshToken);
      tokens.remove(key);
      for (Map.Entry<String, Token> tokenEntry : tokens.entrySet()) {
        if (tokenEntry.getValue().getExpiredAt().compareTo(new Date()) < 0) {
          tokens.remove(tokenEntry.getKey());
          refreshTokens.values().remove(tokenEntry.getKey());
        }
      }
    }
    addToken(token);
  }

  public static Token getToken(String key) {
    Token token;
    if (Constant.cacheEnabled) {
      token = SimpleCache.instance().get(OAUTH2_TOKEN_DEF_KEY, key);
    } else {
      token = tokens.get(key);
      if (token.getExpiredAt().compareTo(new Date()) < 0) {
        tokens.remove(key);
        refreshTokens.values().remove(key);
        token = null;
      }
    }
    return token;
  }

  public static void addRefreshToken(String refreshToken, Token token) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().add(OAUTH2_REFRESH_TOKEN_DEF_KEY, refreshToken, token.getToken(), token.getExpires());
    } else {
      refreshTokens.put(refreshToken, token.getToken());
    }
  }

  public static void removeRefreshToken(String refreshToken) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().remove(OAUTH2_REFRESH_TOKEN_DEF_KEY, refreshToken);
    } else {
      refreshTokens.remove(refreshToken);
    }
  }

  public static Token getTokenByRefreshToken(String refreshToken) {
    String token;
    if (Constant.cacheEnabled) {
      token = SimpleCache.instance().get(OAUTH2_REFRESH_TOKEN_DEF_KEY, refreshToken);

    } else {
      token = refreshTokens.get(refreshToken);
    }
    if (token != null) {
      return getToken(token);
    } else {
      return null;
    }
  }

}
