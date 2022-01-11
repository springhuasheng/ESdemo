package cn.itcast.hotel.constants;

public class GoodsIndexConstants {
    public static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"title\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"copy_to\": \"all\"\n" +
            "      },\n" +
            "      \"price\": {\n" +
            "        \"type\": \"double\"\n" +
            "      },\n" +
            "      \"stock\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"saleNum\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"createTime\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"categoryName\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brandName\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"specStr\": {\n" +
            "        \"type\": \"object\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}