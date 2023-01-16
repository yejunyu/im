create table if not exists 1message_receive
(
    message_id   bigint             not null comment '消息id'
        primary key,
    sender_id    varchar(32)        not null comment '发送者id',
    receiver_id  varchar(32)        not null comment '接受者id',
    group_id     bigint             null comment '群id',
    cmd          int                null comment '请求类型',
    sequence     int                null comment '消息序号',
    content      text               not null comment '消息内容',
    send_time    datetime           null comment '发送时间',
    message_type int                null comment '消息类型',
    is_delivered smallint default 0 not null comment '是否送达;1:送达'
)
    comment '消息接收表';

create table if not exists message_send
(
    message_id   bigint auto_increment comment '消息id'
        primary key,
    sender_id    varchar(32) not null comment '发送者id',
    receiver_id  varchar(32) not null comment '接受者id',
    group_id     bigint      null comment '群id',
    cmd          int         null comment '请求类型',
    sequence     int         null comment '消息序号',
    content      text        not null comment '消息内容',
    send_time    datetime    null comment '发送时间',
    message_type int         null comment '消息类型'
)
    comment '发送表';

