package org.dreamwork.persistence.test;

import org.dreamwork.concurrent.Looper;
import org.dreamwork.db.IDatabase;
import org.dreamwork.db.PostgreSQL;
import org.dreamwork.persistence.DatabaseSchema;
import org.junit.After;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by game on 2017/6/17
 */
public class TestMain {
    private IDatabase db;

    @org.junit.Before
    public void setUp () throws Exception {
//        Looper.create ("test.loop", 16);
        DatabaseSchema.register (new TestDatabaseSchema ());
        DatabaseSchema.register (PostBackLogSchema.class);

        db = new PostgreSQL ("jdbc:postgresql://192.168.2.29/nb_dump", "nb", "nb");
        if (!db.isTablePresent (TestBean.class)) {
            db.createSchemas ();
        }
    }

    @Test
    public void looperTest () {
        for (int i = 0; i < 10; i ++) {
            Looper.runInLoop ("test.loop", new TestRunner ("#" + i));
        }
/*
        try {
            Thread.sleep (10000);
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
*/
    }

    @After
    public void teardown () {
//        Looper.exit ();
//        System.out.println ("shutdown.");
    }

    @Test
    public void testPagination () throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse ("2017-07-09 09:00:00");
        Date end   = sdf.parse ("2017-07-09 12:00:00");
        String sql = "SELECT * FROM post_back_log WHERE ts >= ? AND ts < ? order by ts";
        long ts    = System.currentTimeMillis ();
        for (int i = 1; i <= 3; i ++) {
            List<PostBackLog> list = db.list (
                    PostBackLog.class, i, 5, sql,
                    new Timestamp (start.getTime ()), new Timestamp (end.getTime ()));
            for (PostBackLog log : list) {
                System.out.println (log.getTs ());
            }
            System.out.println ("=========================");
        }
        long now   = System.currentTimeMillis ();
        System.out.println (now - ts);
    }

    private List<TestBean> create (int count) {
        List<TestBean> list = new ArrayList<> (count);
        for (int i = 0; i < count; i ++) {
            TestBean bean = new TestBean ();
            bean.setIntValue (100);
            bean.setLongValue (1000L);
            bean.setName ("test bean");
            bean.setTimestamp (new Date ());
            bean.setMemo (null);
            list.add (bean);
        }

        return list;
    }

    @Test
    public void testSave () throws Exception {
        List<TestBean> list = create (1000);
        System.out.println ("saving without fetching pk");

        long now = System.currentTimeMillis ();
        db.save (list);
        long after = System.currentTimeMillis ();
        System.out.println (after - now);
        System.out.println ();

        list = create (1000);
        System.out.println ("saving with fetching pk");
        now = System.currentTimeMillis ();
        db.save (list, true);
        after = System.currentTimeMillis ();
        System.out.println (after - now);
        System.out.println ();

        System.out.println (">>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println (list.get (0).getId ());
        System.out.println ("<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void testExecute () throws Exception {
        String sql = "DELETE FROM test_table WHERE id > 7";
        db.executeUpdate (sql);

        sql = "DELETE FROM test_table WHERE id = ?";
        db.executeUpdate (sql, 7);
    }

    @Test
    public void testGetByPk () {
        TestBean bean = db.getByPK (TestBean.class, 1);
        System.out.println ("bean.getName () = " + bean.getName ());
    }

    @Test
    public void testGet () {
        List<TestBean> list = db.get (TestBean.class);
        System.out.println (list.size ());

        list = db.get (TestBean.class, "id < ?", 4);
        System.out.println (list.size ());
    }

    @Test
    public void testUpdate () {
        List<TestBean> list = db.get (TestBean.class);
        for (int i = 0; i < list.size (); i ++) {
            list.get (i).setName ("Test Bean #" + (i + 1));
        }
        TestBean[] a = new TestBean[list.size ()];
        a = list.toArray (a);
        db.update (a);
    }

    @Test
    public void testLargeText () {
        TestBean bean = db.getByPK (TestBean.class, 1);
        bean.setName ("The new Name");
        bean.setMemo ("! W6 P4 m: P: |+ K\n" +
                "Video5 V9 d( L0 S% v* F0 \\) I\n" +
                "ID                                       : 1\n" +
                "Format                                   : HEVC\n" +
                "Format/Info                              : High Efficiency Video Coding6 w5 k- e8 K5 `\" K\n" +
                "Format profile                           : Main 10@L5.1@High\n" +
                "Codec ID                                 : V_MPEGH/ISO/HEVC\n" +
                "Duration                                 : 2 h 1 min\n" +
                "Bit rate                                 : 35.6 Mb/s8 |0 q* l$ }; ?5 Z% v\n" +
                "Width                                    : 3 840 pixels: W+ q4 V! w# u% S. L\n" +
                "Height                                   : 2 076 pixels\n" +
                "Display aspect ratio                     : 1.85:1\n" +
                "Frame rate mode                          : Constant\n" +
                "Frame rate                               : 23.976 (24000/1001) FPS\n" +
                "Color space                              : YUV\n" +
                "Chroma subsampling                       : 4:2:0 (Type 2)% ]6 G2 |1 ~0 [& J\" X( t\n" +
                "Bit depth                                : 10 bits/ ?4 j0 ^6 O$ U  h: @\n" +
                "Bits/(Pixel*Frame)                       : 0.186\n" +
                "Stream size                              : 30.2 GiB (78%)\n" +
                "Title                                    : ( Q4 u# w, C% Y2 W\n" +
                "\n" +
                "Inferno.2016.2160p.BluRay.x265.10bit.HDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ\n" +
                "Writing library                          : x265 2.30dbee7ac1e59:[Windows][GCC / X2 Y8 E3 b% v0 N  y  i\n" +
                "\n" +
                "4.9.3][64 bit] 10bit, @\" {1 e  Y3 W\n" +
                "Encoding settings                        : cpuid=1111111 / frame-threads=16 / numa-\n" +
                "\n" +
                "pools=24,24,24,24 / wpp / no-pmode / no-pme / no-psnr / no-ssim / log-level=2 / 4 ]& v% ]* ]5 I\n" +
                "  ]0 S- }2 N5 Y) c/ v& l& V\n" +
                "input-csp=1 / input-res=3840x2076 / interlace=0 / total-frames=0 / level-idc=51 / 1 k. Z+ j0 E' H6 c5 z\n" +
                "\n" +
                "high-tier=1 / uhd-bd=0 / ref=3 / no-allow-non-conformance / repeat-headers / annexb ; k; ]9 _& B0 R1 M\n" +
                "* V4 l. i  ]* z9 g/ {! u9 S\n" +
                "/ aud / hrd / info / hash=0 / no-temporal-layers / no-open-gop / min-keyint=1 / # u+ J7 R# }\" x, M! ?( ]\n" +
                "( I8 D7 k3 [$ u2 F; N\n" +
                "keyint=24 / bframes=4 / b-adapt=2 / b-pyramid / bframe-bias=0 / rc-lookahead=25 / \n" +
                "# m3 {  g# |; x$ P- v8 E\n" +
                "lookahead-slices=4 / scenecut=40 / no-intra-refresh / ctu=64 / min-cu-size=8 / no-0 E( r& l\" D; X1 v3 N\n" +
                "\n" +
                "rect / no-amp / max-tu-size=32 / tu-inter-depth=1 / tu-intra-depth=1 / limit-tu=0 / / l7 B8 l/ V- U0 [  w- d* l\n" +
                "\n" +
                "rdoq-level=0 / dynamic-rd=0.00 / signhide / no-tskip / nr-intra=0 / nr-inter=0 / \n" +
                "$ ?2 T$ f) t+ J  V6 Z2 H6 w$ v3 B9 U* H\n" +
                "no-constrained-intra / strong-intra-smoothing / max-merge=2 / limit-refs=3 / no-\n" +
                "\n" +
                "limit-modes / me=1 / subme=2 / merange=57 / temporal-mvp / weightp / no-weightb /   R! F0 t* K; [- h7 h\n" +
                "  _) Q, d, _# ~4 V% l& b. B\n" +
                "no-analyze-src-pics / deblock=0:0 / sao / no-sao-non-deblock / rd=3 / no-early-skip \n" +
                "! H  }% `) i& y  e7 ]4 f. v+ W- E\n" +
                "/ rskip / no-fast-intra / no-tskip-fast / no-cu-lossless / no-b-intra / rdpenalty=0 0 r; Y% T, y- c! L% ~% C\n" +
                "\n" +
                "/ psy-rd=2.00 / psy-rdoq=0.00 / no-rd-refine / analysis-mode=0 / no-lossless / # |: N* o$ J9 ?5 Q6 S/ }\n" +
                "9 |2 G% `* h) t, U6 ]; l( ~% c% ~\n" +
                "cbqpoffs=0 / crqpoffs=0 / rc=crf / crf=16.0 / qcomp=0.60 / qpstep=4 / stats-write=0 4 [, _) B+ i( l) O$ z1 }5 s\n" +
                "7 d6 k' k' c/ k1 M7 b) W+ z\n" +
                "/ stats-read=0 / vbv-maxrate=160000 / vbv-bufsize=160000 / vbv-init=0.9 / crf-4 [  a* K+ L2 G$ P- C\n" +
                "\n" +
                "max=0.0 / crf-min=0.0 / ipratio=1.40 / pbratio=1.30 / aq-mode=1 / aq-strength=1.00 / 5 Y4 x( P5 ~3 r, k0 l% k' P; s\n" +
                "\n" +
                "cutree / zone-count=0 / no-strict-cbr / qg-size=32 / no-rc-grain / qpmax=69 / \n" +
                "\n" +
                "qpmin=0 / sar=1 / overscan=0 / videoformat=5 / range=0 / colorprim=9 / transfer=16 / \n" +
                "\n" +
                "colormatrix=9 / chromaloc=1 / chromaloc-top=2 / chromaloc-bottom=2 / display-4 h0 N& h\" d' f: G2 ^5 P\n" +
                "+ p7 S  @  i6 |9 n% l, M6 h/ b8 ]& y\n" +
                "window=0 / master-display=G(13250,34500)B(7500,3000)R(34000,16000)WP(15635,16450)L- W\" j8 n7 h; e4 ~* N0 `\n" +
                "$ f: N- q5 s1 o9 L. }: L$ L\n" +
                "(40000000,50) / max-cll=10000,3647 / min-luma=0 / max-luma=1023 / log2-max-poc-lsb=8 \n" +
                "0 P5 U5 i; V6 @3 j  u3 `& h3 \\: M8 G+ _6 U\n" +
                "/ vui-timing-info / vui-hrd-info / slices=1 / opt-qp-pps / opt-ref-list-length-pps / \n" +
                "- I( B7 F% F/ u0 P\n" +
                "no-multi-pass-opt-rps / scenecut-bias=0.05 / no-opt-cu-delta-qp / no-aq-motion / hdr: M# N8 w3 b) U\n" +
                "Language                                 : English6 W  Y* G5 F1 r% g\n" +
                "Default                                  : Yes' k& u2 t; q: n/ Q0 W\n" +
                "Forced                                   : No\n" +
                "Color range                              : Limited! d3 x& P) Y1 A/ e1 j5 m; A\n" +
                "Color primaries                          : BT.2020$ K7 o7 J& n& r2 ^\n" +
                "Transfer characteristics                 : SMPTE ST 2084\n" +
                "Matrix coefficients                      : BT.2020 non-constant; n3 Z7 t/ V& E3 I+ O6 ]6 _! a\n" +
                "Mastering display color primaries        : R: x=0.680000 y=0.320000, G: x=0.265000 3 S9 x6 u6 i  a) m\n" +
                "\n" +
                "y=0.690000, B: x=0.150000 y=0.060000, White point: x=0.312700 y=0.329000\n" +
                "Mastering display luminance              : min: 0.0050 cd/m2, max: 4000.0000 cd/m2\n" +
                "Maximum Content Light Level              : 10000 cd/m2\n" +
                "Maximum Frame-Average Light Level        : 3647 cd/m2\n" +
                "\n" +
                "Audio #18 f' S! U: X7 i# Y. W$ G; R\n" +
                "ID                                       : 2\n" +
                "Format                                   : DTS\n" +
                "Format/Info                              : Digital Theater Systems\n" +
                "Format profile                           : MA / Core1 B7 ]2 m  }6 a) n2 ]7 t* n: B- Q\n" +
                "Mode                                     : 16\n" +
                "Format settings, Endianness              : Big\n" +
                "Codec ID                                 : A_DTS4 ?! I; B: f. n5 G7 t\" s\n" +
                "Duration                                 : 2 h 1 min* P7 x: s1 N) a9 B7 w8 O% J2 p/ U  N\n" +
                "Bit rate mode                            : Variable / Constant0 N! Q8 w0 M/ F) a' V/ D5 u, \\\n" +
                "Bit rate                                 : 4 060 kb/s / 1 509 kb/s8 o, P\" N, y% E+ P2 c+ a2 y\n" +
                "Channel(s)                               : 8 channels / 6 channels0 z* M/ o\" f2 A8 {* a\n" +
                "Channel positions                        : Front: L C R, Side: L R, Back: L R, LFE / \n" +
                "\n" +
                "Front: L C R, Side: L R, LFE\n" +
                "Sampling rate                            : 48.0 kHz\n" +
                "Frame rate                               : 93.750 FPS (512 spf). m8 b2 D6 B3 w- ?+ ^' U; M\" C\n" +
                "Bit depth                                : 24 bits\n" +
                "Compression mode                         : Lossless / Lossy\n" +
                "Stream size                              : 3.45 GiB (9%)\n" +
                "Title                                    : ; m% Z  \\; Q3 E7 M\n" +
                "8 l2 G0 w, s. k: ~5 j( l) Z\n" +
                "Inferno.2016.2160p.BluRay.x265.10bit.HDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ# ?  x& g% H\" [+ k! n\n" +
                "Language                                 : English\n" +
                "Default                                  : Yes\n" +
                "Forced                                   : No1 v3 C3 c4 C  d9 u, ?\" z% v\n" +
                "\n" +
                "Audio #29 }\" i3 ?' _5 Z' l# e& l1 t\n" +
                "ID                                       : 3\n" +
                "Format                                   : TrueHD\n" +
                "Format profile                           : TrueHD+Atmos / TrueHD\n" +
                "Codec ID                                 : A_TRUEHD\n" +
                "Duration                                 : 2 h 1 min\n" +
                "Bit rate mode                            : Variable\n" +
                "Bit rate                                 : 4 900 kb/s\n" +
                "Maximum bit rate                         : 8 316 kb/s4 p4 c( b& B. O; j\n" +
                "Channel(s)                               : Object Based / 8 channels' r\" N! y! Z& K6 r\n" +
                "Channel positions                        : Object Based / Front: L C R, Side: L R, . S% v$ j8 i2 ?& q/ u7 I\n" +
                ") I4 `7 f  w3 ]6 h\n" +
                "Back: L R, LFE\n" +
                "Sampling rate                            :  / 48.0 kHz% p/ P( A& _9 Y8 G\n" +
                "Frame rate                               : 1 200.000 FPS (40 spf), G* e8 S! g5 |1 y& ]% t\" b\n" +
                "Compression mode                         : Lossless! b- g! }) E% S4 M3 {3 w/ `\n" +
                "Delay relative to video                  : 26 ms\n" +
                "Stream size                              : 4.16 GiB (11%)- P, `5 b3 P! e& s3 a# w! H\n" +
                "Title                                    : 7 e* D8 Z7 X1 l' h3 ?\n" +
                "; T/ J  R7 y5 X1 V& v1 |  z( k\n" +
                "Inferno.2016.2160p.BluRay.x265.10bit.HDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ) t) m, ]0 N* `\" N% E2 R: d\n" +
                "Language                                 : English; x2 b3 f) T8 W+ x( {\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Audio #3\n" +
                "ID                                       : 4! J0 J$ y3 E  E( Q\n" +
                "Format                                   : AC-35 f, q6 U) L  r\n" +
                "Format/Info                              : Audio Coding 3\n" +
                "Format settings, Endianness              : Big! Q1 ]3 x+ m1 w2 X\n" +
                "Codec ID                                 : A_AC3\n" +
                "Duration                                 : 2 h 1 min  k' C# G: K1 o3 G) T\" S\n" +
                "Bit rate mode                            : Constant\n" +
                "Bit rate                                 : 640 kb/s; O; g4 {1 |$ b2 n6 s& D( X\n" +
                "Channel(s)                               : 6 channels\n" +
                "Channel positions                        : Front: L C R, Side: L R, LFE\n" +
                "Sampling rate                            : 48.0 kHz\n" +
                "Frame rate                               : 31.250 FPS (1536 spf)4 D: L; w/ x& R' N5 c\n" +
                "Bit depth                                : 16 bits\" `. J# o7 j% k2 a4 I\n" +
                "Compression mode                         : Lossy# f/ V\" {) z- s8 @. c\n" +
                "Delay relative to video                  : 26 ms( E9 M6 t  r4 k8 m\n" +
                "Stream size                              : 557 MiB (1%)  A\" m) P\" `& c3 \\% h\n" +
                "Title                                    : \n" +
                "\n" +
                "Inferno.2016.2160p.BluRay.x265.10bit.HDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ% ]% g- D' c- `3 J; W. y7 a\n" +
                "Language                                 : English\n" +
                "Service kind                             : Complete Main( @# O! \\7 b  Q4 J3 w\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #1\n" +
                "ID                                       : 5\n" +
                "Format                                   : UTF-8  i7 a: M! `7 q\n" +
                "Codec ID                                 : S_TEXT/UTF83 T8 i+ _3 f  O' a\" h3 w\n" +
                "Codec ID/Info                            : UTF-8 Plain Text/ T. l$ L. h) I6 x& e5 S\n" +
                "Duration                                 : 1 h 55 min\n" +
                "Bit rate                                 : 55 b/s( j. D( N1 u+ e\n" +
                "Count of elements                        : 13602 S, Z6 v6 `! h& E\n" +
                "Stream size                              : 46.5 KiB (0%)4 C' J; u6 U1 ~( K6 v  H\n" +
                "Title                                    : English-SRT% E7 t( n2 d9 N2 S# c% ^( ~\n" +
                "Language                                 : English+ b3 l: \\' }8 M& `# X\n" +
                "Default                                  : Yes\n" +
                "Forced                                   : No\n" +
                "/ N' g% o9 C# w% X7 ~% U\n" +
                "Text #2\n" +
                "ID                                       : 6\n" +
                "Format                                   : PGS5 w2 L) x  Z, r2 E\n" +
                "Muxing mode                              : zlib: X7 H$ J: W( \\\n" +
                "Codec ID                                 : S_HDMV/PGS, m; i' T9 `' ?, v\n" +
                "Codec ID/Info                            : Picture based subtitle format used on / Q) F' v- J! I8 h6 D$ r\n" +
                "3 d\" }* Q0 {0 C- z2 D9 f2 `\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 1 h 55 min/ @# o2 A' Q\" \\& ]5 d: L0 d\n" +
                "Bit rate                                 : 16.4 kb/s\n" +
                "Count of elements                        : 2720+ m. j# {% [  |! U- o- b\n" +
                "Stream size                              : 13.5 MiB (0%)\n" +
                "Title                                    : English-PGS& P9 k6 S* N  F) z0 f6 r- ^: i% q. z\n" +
                "Language                                 : English\n" +
                "Default                                  : No\n" +
                "Forced                                   : No6 C) d0 X  q  N\n" +
                "\n" +
                "Text #3$ x4 \\\" D6 B( d7 ?\" S8 h- H\n" +
                "ID                                       : 74 ?. K+ C, ?& s- b  p\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS9 ~! j; w( O$ p8 J  v\n" +
                "Codec ID/Info                            : Picture based subtitle format used on 8 g/ A6 q4 n8 m\" v! l7 W\n" +
                "( k: M\" Z  H5 x& ?5 `' J8 `$ x\n" +
                "BDs/HD-DVDs6 \\$ `4 n) |$ q2 ?2 @4 G\n" +
                "Duration                                 : 1 h 57 min+ i5 z- ~3 J/ Z. W6 [7 T\n" +
                "Bit rate                                 : 18.6 kb/s\n" +
                "Count of elements                        : 3332. S) q3 u( J6 T$ j/ Z( a\n" +
                "Stream size                              : 15.6 MiB (0%)6 ^! A' t- w0 o\" L* m0 z/ f3 _\n" +
                "Title                                    : English-SDH-PGS\n" +
                "Language                                 : English8 _$ q  D& E& _! U- s0 U& ~, c\n" +
                "Default                                  : No+ ^+ o% l2 y' H. }\" [& K( j5 o\n" +
                "Forced                                   : No\n" +
                ". p, J% p( m9 ^8 [  X. J6 _\n" +
                "Text #4\n" +
                "ID                                       : 8+ X. O6 _$ v9 ?* N, C\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on % B4 e  D  W- D( o% m\n" +
                "\n" +
                "BDs/HD-DVDs; h9 W# e' D% k, }% x8 W\n" +
                "Duration                                 : 1 h 55 min\n" +
                "Bit rate                                 : 11.2 kb/s\n" +
                "Count of elements                        : 2788\n" +
                "Stream size                              : 9.24 MiB (0%)\n" +
                "Title                                    : Arabic-PGS9 C$ e4 o9 a0 |) o/ @* q( B\n" +
                "Language                                 : Arabic% o4 [: `& {\" q( O\" }% X\n" +
                "Default                                  : No4 o9 F; R) A! v7 {5 S3 g9 q* I\n" +
                "Forced                                   : No9 H8 J# N& J9 Q' m% U$ k/ D( X  H\n" +
                "\" ^+ e( s2 X( ~% Z\n" +
                "Text #5\n" +
                "ID                                       : 9\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib8 y4 F+ l$ ~* r- ]\n" +
                "Codec ID                                 : S_HDMV/PGS& d5 w# A; H3 r2 T\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 1 h 55 min\n" +
                "Bit rate                                 : 20.2 kb/s6 v: S: q* y7 c8 R: }8 s+ M0 G\n" +
                "Count of elements                        : 2784\n" +
                "Stream size                              : 16.6 MiB (0%)+ t# ~. h7 ?/ R# `\n" +
                "Title                                    : Chinese-PGS$ r0 V1 h( a3 M  e2 u# T, n\n" +
                "Language                                 : Chinese\n" +
                "Default                                  : No1 p, i9 q& F% n) V2 C\n" +
                "Forced                                   : No\n" +
                ". l& ]$ u  z5 ?/ J* u* H\n" +
                "Text #6\n" +
                "ID                                       : 106 ?\" g  @. J\" e! |  _\n" +
                "Format                                   : PGS# o0 S- ~+ |/ ~' _\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS5 o2 E% |' A8 i0 v4 x\n" +
                "Codec ID/Info                            : Picture based subtitle format used on : X# D) p1 B9 m- {% _0 O\n" +
                "\n" +
                "BDs/HD-DVDs$ H6 a+ H4 V7 y* R0 }\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 15.4 kb/s\n" +
                "Count of elements                        : 2758\n" +
                "Stream size                              : 13.3 MiB (0%)\n" +
                "Title                                    : Czech-PGS+ U$ f2 q- x' F, @. @5 x\n" +
                "Language                                 : Czech\n" +
                "Default                                  : No; q/ f- W9 N  g2 s5 z$ ?\" m; i\n" +
                "Forced                                   : No2 l- Q; A$ b2 Q6 f8 w  }* f\n" +
                "' @  X% _4 f% y) v\n" +
                "Text #74 r1 w! @# z7 d3 @) }( c+ ]\n" +
                "ID                                       : 11* R' T! _) c; X3 A. v9 B9 E\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib; J' i7 O4 f0 h$ J\n" +
                "Codec ID                                 : S_HDMV/PGS/ k. ~- C2 k! r2 S\n" +
                "Codec ID/Info                            : Picture based subtitle format used on 2 V2 q6 s% A1 |1 t; q7 R; L9 N& V\n" +
                "6 U0 @$ a4 n. K  `+ h1 L& c\n" +
                "BDs/HD-DVDs- M) l! g5 R$ ^\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 14.4 kb/s( J3 G) t. _% l$ h# @\n" +
                "Count of elements                        : 2298+ n\" |\" u. Q' I\" T\n" +
                "Stream size                              : 12.4 MiB (0%)\" |' R+ I( y2 L\n" +
                "Title                                    : Dutch-PGS\n" +
                "Language                                 : Dutch) B% |  b\" F% T5 T, I\n" +
                "Default                                  : No\n" +
                "Forced                                   : No- L5 M1 `3 y9 c# u4 r\n" +
                ") _0 E/ I7 S. H. U! }\n" +
                "Text #8\n" +
                "ID                                       : 12\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib0 d% A0 o* A+ L7 L' S+ |\n" +
                "Codec ID                                 : S_HDMV/PGS. R) ?+ F7 s0 W  W\" y\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs) P% J$ G2 {) I\n" +
                "Duration                                 : 2 h 0 min0 t% I) _* w6 \\\n" +
                "Bit rate                                 : 14.4 kb/s\n" +
                "Count of elements                        : 2298\n" +
                "Stream size                              : 12.4 MiB (0%)8 d( _; z( `5 d\n" +
                "Title                                    : Dutch-PGS\n" +
                "Language                                 : Dutch& Y\" n& v# a* N, j( m+ X\n" +
                "Default                                  : No\n" +
                "Forced                                   : No# J1 Z9 Q* |5 H0 ]) D) l' N\n" +
                "7 B7 m4 d0 S% R3 b4 M\n" +
                "Text #9- V/ o8 X2 S6 K2 {1 @3 m5 ^\n" +
                "ID                                       : 131 ?! m3 S- E* u+ l6 I9 F\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS5 L( e% ?3 f6 v) t1 d& l7 E7 J\n" +
                "Codec ID/Info                            : Picture based subtitle format used on % Q, G$ q9 N- q/ a\n" +
                "2 Z\" W, |& y, i; A\n" +
                "BDs/HD-DVDs/ O1 ^$ x& E; W: U$ d. t\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 14.0 kb/s\n" +
                "Count of elements                        : 3068\" \\% s) v+ m. {: T1 s\n" +
                "Stream size                              : 12.1 MiB (0%)\n" +
                "Title                                    : Estonian-PGS; h) Z1 n3 q/ }. l\n" +
                "Language                                 : Estonian\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #10\n" +
                "ID                                       : 14\n" +
                "Format                                   : PGS8 @4 D3 ]- n# W\n" +
                "Muxing mode                              : zlib6 u4 s) G3 U+ [\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on ) Y1 ?% {2 g7 V3 B: I! |\n" +
                "\n" +
                "BDs/HD-DVDs7 v$ E& Q0 y( \\1 z% V- ]- G7 _\n" +
                "Duration                                 : 2 h 0 min9 a3 v: y5 N- _\n" +
                "Bit rate                                 : 14.1 kb/s' A. v, A' ^! j: T# ^; O  n* \\  c* O\n" +
                "Count of elements                        : 2756\n" +
                "Stream size                              : 12.2 MiB (0%)! [1 R& C& H! p) C1 W, ]8 U\n" +
                "Title                                    : Finnish -PGS\n" +
                "Language                                 : Finnish\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #11\n" +
                "ID                                       : 15$ B$ \\. ?, H' ~2 v\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS% {' T/ w- F9 z1 n8 Q( v\n" +
                "Codec ID/Info                            : Picture based subtitle format used on ! P. B+ r. m3 I\n" +
                "  b' t% S+ a6 k\n" +
                "BDs/HD-DVDs2 q0 l' M6 S3 A; E' R2 q2 z: P\n" +
                "Duration                                 : 2 h 0 min% n8 N* o5 F0 a: r9 Z\n" +
                "Bit rate                                 : 16.3 kb/s' Q6 d* A; W% H/ m, h' ]\n" +
                "Count of elements                        : 3086\n" +
                "Stream size                              : 14.0 MiB (0%)\n" +
                "Title                                    : French-PGS3 ?7 E0 o2 D7 n/ Y\n" +
                "Language                                 : French5 E& z& J, }. Z\" t\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "; u6 c% i: X4 }! N$ s- G; e- j. A\n" +
                "Text #12( c- ]/ m% l* J& L, S6 c6 O) ^\n" +
                "ID                                       : 16. O\" l5 m9 a) A\n" +
                "Format                                   : PGS3 K1 n9 K: S1 H% `( w; r\n" +
                "Muxing mode                              : zlib* }+ l. I/ Q! f4 e: g( S/ m. X\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "0 ~' |- H* f! t, r% i: c\n" +
                "BDs/HD-DVDs8 ~6 \\' C7 n# I9 j  }) E\n" +
                "Duration                                 : 2 h 0 min5 d' x% v6 ~3 D% E( z1 G! m3 M. ]\n" +
                "Bit rate                                 : 17.3 kb/s\n" +
                "Count of elements                        : 2836\n" +
                "Stream size                              : 14.9 MiB (0%); b6 r, r& z: \\5 e\n" +
                "Title                                    : German-PGS) Z% X* Y6 k8 c; s\" I- S) N; w% c\n" +
                "Language                                 : German9 q9 |; O2 ?  N3 J0 f: W% M% ^6 z5 t\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #136 }* u; y, t4 ~4 |: L* y. c& _& o\n" +
                "ID                                       : 17( z# B3 w. _/ Z& b9 d  H, `\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib. \\5 x1 D8 s2 l7 Z  U) i\n" +
                "Codec ID                                 : S_HDMV/PGS# T' ?; z' G  E( p. a- J\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \" Y* X\" W, o' n. a\" D\n" +
                "\n" +
                "BDs/HD-DVDs& s. X; V6 C6 i( I3 n2 j- b\" x* ^. B% Y2 T\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 15.4 kb/s2 R  f8 K5 D; S\n" +
                "Count of elements                        : 2792% {, @+ g8 w' i0 i1 |) y! G\n" +
                "Stream size                              : 13.2 MiB (0%)\n" +
                "Title                                    : Hungarian-PGS/ a2 k  }8 M* T, N7 H3 R\n" +
                "Language                                 : Hungarian. u# d# }4 @9 q$ o/ |\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\" A9 b! g- \\. z: D\n" +
                "\n" +
                "Text #14\n" +
                "ID                                       : 18: X' D, Y# `7 i\" C$ L\n" +
                "Format                                   : PGS& |, z. X$ n1 t( F& {) b4 d/ y\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS, [3 a' Q& |; w/ q\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 1 h 55 min+ t& g0 |7 k4 @5 n5 Q; [. Q\n" +
                "Bit rate                                 : 16.1 kb/s- ?5 W9 E0 |1 h. r9 \\* @* b$ s9 W4 H\n" +
                "Count of elements                        : 2784! Q6 J1 ^0 a# e& `3 B\n" +
                "Stream size                              : 13.2 MiB (0%)\n" +
                "Title                                    : Italian-PGS% O; a+ G* l! V8 g% D3 A\n" +
                "Language                                 : Italian\n" +
                "Default                                  : No\n" +
                "Forced                                   : No8 o. I6 p! H4 a7 f( ]; {$ Y\n" +
                "& F4 o9 D+ h. ^, g1 A/ [\n" +
                "Text #15  a\" ^0 E& ~# F& ^2 G2 B\n" +
                "ID                                       : 19\n" +
                "Format                                   : PGS: y( `( k5 N5 L5 H1 p: K& t\n" +
                "Muxing mode                              : zlib4 M* F9 N( P, p& z\n" +
                "Codec ID                                 : S_HDMV/PGS& K' G: j\" n! a: H\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs) l, Z! a5 I2 S7 ]\n" +
                "Duration                                 : 2 h 0 min' N, t& R* o( C# M6 t7 K2 F\n" +
                "Bit rate                                 : 15.1 kb/s\n" +
                "Count of elements                        : 26680 R9 _: A3 N; l% g\n" +
                "Stream size                              : 13.0 MiB (0%)\n" +
                "Title                                    : Japanese-PGS, ^\" r& M. O6 `* j0 e: F\n" +
                "Language                                 : Japanese\n" +
                "Default                                  : No\n" +
                "Forced                                   : No1 Z) n; t: n4 y; A$ X: ?( X. H9 c+ C& _\n" +
                "\n" +
                "Text #16. j' W2 v8 W8 V& ]; n\n" +
                "ID                                       : 20\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib/ n) }( x6 {( ]! _\n" +
                "Codec ID                                 : S_HDMV/PGS7 t\" C& ~& v7 @% h1 q\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 1 h 54 min\n" +
                "Bit rate                                 : 11.2 kb/s\" {/ W' V, p% `2 r2 Z5 _; K\n" +
                "Count of elements                        : 2746/ \\8 y6 D. r) T\n" +
                "Stream size                              : 9.20 MiB (0%)\n" +
                "Title                                    : Korean-PGS\n" +
                "Language                                 : Korean8 R- q- ^; O2 H. Z- T\n" +
                "Default                                  : No  U\" ?! o9 M; }$ P. d\" N4 r5 V- f\n" +
                "Forced                                   : No\n" +
                "+ ^0 _/ S* |/ k, c+ l\n" +
                "Text #17\n" +
                "ID                                       : 216 ^+ A/ T$ g! T0 |% `8 S, z6 @2 ]\n" +
                "Format                                   : PGS9 P- r2 n& w. Y& F/ M& }9 m1 H\" l\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on . }2 u& L2 M* Z# ~2 X\n" +
                "# B! V& w. c4 }& h+ C- W\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 1 h 55 min, d7 i) g- m* i% l8 ]\n" +
                "Bit rate                                 : 13.0 kb/s  U) x- G7 }9 F  j; j. L\n" +
                "Count of elements                        : 2806+ f4 t, [0 q1 Q9 U5 |\n" +
                "Stream size                              : 10.7 MiB (0%)\n" +
                "Title                                    : Latvian-PGS\n" +
                "Language                                 : Latvian\n" +
                "Default                                  : No5 ]) a- H% a: y) r  c/ i3 Z: ]6 d6 s\n" +
                "Forced                                   : No5 i' {1 l\" l4 m+ k; B- j\n" +
                "% I2 J\" [; v# X4 p6 x* T\n" +
                "Text #187 w( W\" h; N% D' l$ i+ F\n" +
                "ID                                       : 22* A0 ?) W  V: ~5 P' c\n" +
                "Format                                   : PGS4 X8 [% P7 f+ b& h% j/ i& k; |3 {. j\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on ( _1 l( g5 n+ m: ]* w\n" +
                "& ?, Q6 c: W4 O' E\n" +
                "BDs/HD-DVDs\" h) Q- _' a5 x0 o\" X8 ?4 b  p\n" +
                "Duration                                 : 1 h 55 min& C' t* a! y0 r4 y/ M$ f\n" +
                "Bit rate                                 : 14.6 kb/s( F/ s: n; n/ W6 D  D' Q8 e$ \\\n" +
                "Count of elements                        : 2916) p! H8 y. v& W0 S6 K\n" +
                "Stream size                              : 12.0 MiB (0%)\n" +
                "Title                                    : Lithuanian-PGS\n" +
                "Language                                 : Lithuanian\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "9 A* ^: |  I2 i0 c\n" +
                "Text #19  e\" l, g6 G! a& j\n" +
                "ID                                       : 23! E5 x8 r* S) s\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on + z; S1 l6 `/ ^9 Q- b8 b. J\n" +
                "5 {/ ]7 w\" w5 Q) J9 q, `\n" +
                "BDs/HD-DVDs9 t$ T8 ]1 U+ y: B0 k0 d4 w0 u\n" +
                "Duration                                 : 1 h 55 min1 e$ R# X) l. d' o\n" +
                "Bit rate                                 : 16.7 kb/s. Y5 s/ ?: t6 R& d# E! y. X\n" +
                "Count of elements                        : 2774/ c* B& I7 X  G9 q  Y% I) _8 s\n" +
                "Stream size                              : 13.7 MiB (0%)\n" +
                "Title                                    : Norwegian-PGS/ `' Y; _; F5 v. d# Y* V\n" +
                "Language                                 : Norwegian\n" +
                "Default                                  : No4 Z2 i: n, A4 n- o4 q; W\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #20\n" +
                "ID                                       : 240 n. Y, T- d4 Q\n" +
                "Format                                   : PGS; p2 m/ j\" o8 u1 L- x, B\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on - X; ]- N7 K6 k- Y\" L0 L\n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min! ^7 H5 D% H9 W9 N\n" +
                "Bit rate                                 : 13.4 kb/s\n" +
                "Count of elements                        : 2620\n" +
                "Stream size                              : 11.6 MiB (0%)\n" +
                "Title                                    : Polish-PGS\n" +
                "Language                                 : Polish\n" +
                "Default                                  : No1 ?, V4 l( F- Y' z\" ~. V\n" +
                "Forced                                   : No\n" +
                "6 E: s9 u1 \\\" b; e+ {\n" +
                "Text #21# c$ g% l$ S! {\n" +
                "ID                                       : 255 t, l0 R1 {: B- J\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib9 F3 B1 ~# z: g0 t\n" +
                "Codec ID                                 : S_HDMV/PGS, @7 q! [0 E6 q2 c4 k\n" +
                "Codec ID/Info                            : Picture based subtitle format used on - t6 F/ X( s5 ?. t) h( e\n" +
                "\n" +
                "BDs/HD-DVDs# G: _, |. P. u4 N, c\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 17.0 kb/s; N/ D  m, U; l\" ~$ P0 ]( x# I\n" +
                "Count of elements                        : 27420 k\" J/ \\' }3 @  i2 d. M\n" +
                "Stream size                              : 14.7 MiB (0%); v; h2 p+ E! a' A# k# P, ?. x\n" +
                "Title                                    : Portuguese6 z3 ~\" }0 y, p( J4 K( Z\n" +
                "Language                                 : Portuguese\n" +
                "Default                                  : No\n" +
                "Forced                                   : No) E5 T& R# S  J. @\" Q2 I\n" +
                "\n" +
                "Text #22) S7 H# }1 ]% _% s; Y5 _- P\n" +
                "ID                                       : 26) d; n- }2 A\" _9 h' O% g\n" +
                "Format                                   : PGS3 ^7 E7 s  D: I5 O: ]\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min$ o; y3 X& r7 S7 u\n" +
                "Bit rate                                 : 16.2 kb/s\" B: D% h' V& v' ]6 H/ m$ Y\n" +
                "Count of elements                        : 2717: U& V+ |1 R2 u$ |# ?* e\n" +
                "Stream size                              : 14.0 MiB (0%)\n" +
                "Title                                    : Portuguese. m' t: P# ^# k, s0 L, K\n" +
                "Language                                 : Portuguese: W2 B) w5 X/ E  c0 t1 V9 P% A\n" +
                "Default                                  : No1 a6 r\" x( t9 b0 z8 f- Q/ B$ a( a3 ?\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #23\n" +
                "ID                                       : 27\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\" j2 [/ N( k5 N\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "1 e7 Z  J4 ^. g  t\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 14.8 kb/s; u6 R3 w2 N7 a8 ]0 j# H2 {\n" +
                "Count of elements                        : 2724# c+ ?7 m2 y- I\n" +
                "Stream size                              : 12.8 MiB (0%)\n" +
                "Title                                    : Romanian: W+ O0 ?2 i  ]4 v, K$ n\n" +
                "Language                                 : Romanian\n" +
                "Default                                  : No' n( K, G* }! Z  F! m7 W( r8 d# D\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #24$ k0 Q& q# w- r1 a* X8 j\n" +
                "ID                                       : 287 X) E\" U6 Y* [' P: X5 ^9 c5 D\n" +
                "Format                                   : PGS: q# r7 L7 x3 ~4 m; [- s\" M\n" +
                "Muxing mode                              : zlib6 ^0 v  `9 }\" y! z) j' ~3 u\n" +
                "Codec ID                                 : S_HDMV/PGS1 b4 m$ D  Q; o/ c1 H2 Q- d6 e! s\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 1 h 55 min% O$ j* n  j/ d# W' {& `5 u\n" +
                "Bit rate                                 : 16.4 kb/s\n" +
                "Count of elements                        : 27989 M6 [7 x8 Y7 Q! i' b' X$ M\n" +
                "Stream size                              : 13.5 MiB (0%)7 r6 v0 X4 ^+ `/ l; B\" ?\n" +
                "Title                                    : Russian-PGS\n" +
                "Language                                 : Russian\n" +
                "Default                                  : No' k\" A\" K4 s0 h# V9 Q# q% a\n" +
                "Forced                                   : No\n" +
                "5 \\7 U. ~) Z# L8 n\n" +
                "Text #25\n" +
                "ID                                       : 29( d' ]\" V) B$ V\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS# m5 _9 \\6 X6 h, [* K\" x) e\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min! E/ H6 R; w6 y9 t\n" +
                "Bit rate                                 : 15.2 kb/s\n" +
                "Count of elements                        : 2752' [- l2 l, @; k* `; Z) b0 @' r\n" +
                "Stream size                              : 13.1 MiB (0%)8 w3 L1 R) y+ G* a6 `9 D& N\n" +
                "Title                                    : Spanish-PGS\n" +
                "Language                                 : Spanish\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #26% W6 V- s8 |1 j  R& w\n" +
                "ID                                       : 30\n" +
                "Format                                   : PGS: [4 r3 {6 ]  _7 z: [\" o# X\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on , Z5 r! A2 Z, L& i' e  P- I\n" +
                "\n" +
                "BDs/HD-DVDs, R* R$ h# G% @7 G; E\" B\" c+ p\n" +
                "Duration                                 : 2 h 0 min! b$ t/ l+ Q4 }\" q! v$ Z2 O$ \\, o\n" +
                "Bit rate                                 : 15.3 kb/s\n" +
                "Count of elements                        : 2804\n" +
                "Stream size                              : 13.1 MiB (0%)( \\' d- `; M; ]6 k; \\\n" +
                "Title                                    : Spanish-PGS3 e7 }\" _0 \\# F+ @9 @/ V9 a\n" +
                "Language                                 : Spanish\n" +
                "Default                                  : No\n" +
                "Forced                                   : No; }) h) K  g+ T& M' q, l  E\n" +
                "\n" +
                "Text #27: K; q  s1 [3 V* S9 `, M\n" +
                "ID                                       : 31\n" +
                "Format                                   : PGS3 J% I\" J0 W/ V\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min/ a3 t. Y3 S/ O/ \\- T/ b$ Y\n" +
                "Bit rate                                 : 14.0 kb/s) }/ B& \\7 k: w\" `$ \\# W* j7 R\n" +
                "Count of elements                        : 2447\n" +
                "Stream size                              : 12.1 MiB (0%)4 Q+ }2 ^1 W! m( f6 Y  o' ^\n" +
                "Title                                    : Swedish-PGS4 {2 B  [8 a: |2 D/ ^9 N\n" +
                "Language                                 : Swedish\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                "\n" +
                "Text #28) s! u+ a. V. `% {& t7 M  f\n" +
                "ID                                       : 32! ]! F/ Q% }6 B! ?' \\3 Q$ g\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS+ k5 \\; k+ Y3 ^\n" +
                "Codec ID/Info                            : Picture based subtitle format used on \n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min, u\" \\0 G) k4 P& i2 {( K\n" +
                "Bit rate                                 : 15.7 kb/s\n" +
                "Count of elements                        : 2796\n" +
                "Stream size                              : 13.5 MiB (0%)/ _3 K/ v! h  g% H\n" +
                "Title                                    : Thai-PGS\n" +
                "Language                                 : Thai\" x0 g1 s$ ^6 Z0 ?! o/ ^\n" +
                "Default                                  : No\n" +
                "Forced                                   : No\n" +
                ": _* ]( S\" X3 K1 n$ s\" N\n" +
                "Text #29\n" +
                "ID                                       : 33\n" +
                "Format                                   : PGS\n" +
                "Muxing mode                              : zlib\n" +
                "Codec ID                                 : S_HDMV/PGS& m% N  e' l1 R: z  e$ d0 {\n" +
                "Codec ID/Info                            : Picture based subtitle format used on 6 m1 W% T4 b/ ~! P6 V. b& F; W% `\n" +
                "\n" +
                "BDs/HD-DVDs\n" +
                "Duration                                 : 2 h 0 min\n" +
                "Bit rate                                 : 15.0 kb/s  Q9 Y0 T. O4 G\n" +
                "Count of elements                        : 2774/ q/ l\" Y+ c1 @. C: p\n" +
                "Stream size                              : 12.9 MiB (0%)\n" +
                "Title                                    : Turkish-PGS5 N, j6 w8 N5 |. W8 }\n" +
                "Language                                 : Turkish\n" +
                "Default                                  : No- Y/ C3 k1 i- l: t$ E0 R\n" +
                "Forced                                   : No( _% i, I9 \\3 n6 V% P- \\5 |5 [");
        db.update (bean);
    }

    @Test
    public void testDelete () {
        List<TestBean> list = new ArrayList<> ();
        for (int i = 0; i < 10; i ++) {
            TestBean bean = new TestBean ();
            bean.setName ("TB #10" + i);
            bean.setIntValue (i);
            bean.setLongValue (i);
            list.add (bean);
        }
        db.save (list, true);

        TestBean bean = list.remove (0);
        System.out.println ("deleting the bean: #" + bean.getId () + "," + bean.getName ());
        db.delete (bean);

        List<Long> ids = new ArrayList<> (list.size ());
        for (TestBean tb : list) ids.add (tb.getId ());
        db.delete (TestBean.class, ids);
    }
}