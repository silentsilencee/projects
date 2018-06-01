console.log("loading calibration app...")

// this is for the pool plane canvas
var width = 702+13*2,
    height = 520+13*2,
    resolution = 13,
    r = 15;
var camera_facing = 'left';
var num_pts_image = 8; // two extras for image view because of top of cage
var num_pts_ground = num_pts_image-2;

// this is for the image view
// input file index
var input_index = 0;
// input folder path
var game_folder = '20180127_CHNvCAL'
var folder_path = '/static/imgs/'+game_folder+'/';
var annotation_path = '/static/annotation/'+game_folder+'/';
console.log('folder path');
console.log(folder_path); //$("#folder").text();
var total_file_num = 67 //parseInt($("#total_num").text());
//console.log(getCount(folder_path));

// current index of the marker in image plane
var img_index = 0;

var img_labels = [];
var ground_labels =[];

/*var drag = d3.behavior.drag()
    .origin(function(d) { return d; })
    .on('drag', dragged);*/

var drag = d3.behavior.drag()
    .on('dragstart',gt_dragstart)
    .on('drag',gt_dragmove)
    .on('dragend',gt_dragend);

function gt_dragstart(d) {
  d3.select(this).classed("active", true);
}

function gt_dragmove(d) {
  var x = d3.event.x,
      y = d3.event.y,
      gridX = round(Math.max(r, Math.min(width - r, x)), resolution),
      gridY = round(Math.max(r, Math.min(height - r, y)), resolution);
  //console.log(gridX);

  d3.select(this).attr('cx', d.x = gridX).attr('cy', d.y = gridY);
}

function gt_dragend(d) {
    var idx = parseInt(d.id[2]);
    var coordinates = d3.mouse(svg.node());
    //console.log(img_labels)
    ground_labels[idx][0] = coordinates[0]-resolution;
    ground_labels[idx][1] = coordinates[1]-resolution;
    /*console.log(d.id);
    console.log(idx);
    console.log('image index');
    console.log(img_index);*/
    d3.select(this).classed("active", false);
    $( "#submitBtn" ).click();
}

var drag_img = d3.behavior.drag()
             .on("dragstart", dragstart)
             .on("drag", dragmove)
             .on("dragend",dragend);

function dragstart(d) {
  d3.select(this).classed("active", true);
}

function dragmove(d) {
  d3.select(this).attr("cx", d3.event.x).attr("cy", d3.event.y);
}

function dragend(d) {
    var idx = 0;
    var coordinates = d3.mouse(svg_image.node());
    idx = parseInt(d.id[2]);
    //console.log(img_labels)
    img_labels[idx][0] = coordinates[0];
    img_labels[idx][1] = coordinates[1];
    /*console.log(d.id);
    console.log(idx);
    console.log('image index');
    console.log(img_index);*/
    d3.select(this).classed("active", false);
    if (img_index == num_pts_image){
        $( "#submitBtn" ).click();
    }
}

var field_points;
var image_points;

// this just draws the overhead view of the pool, so we can drag on a grid.
var pool_plane = [
  {'x':0+1,'y':17+1,'width':2,'height':6,'fill':'powderblue'},      // left cage
  {'x':27*2-2+1,'y':17+1,'width':2,'height':6,'fill':'powderblue'}, // right cage
  {'x':2+1,'y':0+1,'width':2*2,'height':20*2,'fill':'lightcoral'},
  {'x':28*2-3-4,'y':0+1,'width':2*2,'height':20*2,'fill':'lightcoral'},
  {'x':2+5,'y':0+1,'width':3*2,'height':20*2,'fill':'lightgoldenrodyellow'},
  {'x':28*2-3-4-6,'y':0+1,'width':3*2,'height':20*2,'fill':'lightgoldenrodyellow'},
  {'x':2+5+6,'y':0+1,'width':7.5*2*2,'height':20*2,'fill':'palegreen'},
  {'x':2+11+14+0.75,'y':0+1,'width':0.5,'height':20*2,'fill':'white'}
  ]

$( "#camera_left" ).click(function() {
    camera_facing = 'left';
    reset_template();
    $( "#submitBtn" ).click();
    $("#camera_left").addClass('active');
    $("#camera_right").removeClass('active');
});

$( "#camera_right" ).click(function() {
    camera_facing = 'right';
    reset_template();
    $( "#submitBtn" ).click();
    $("#camera_right").addClass('active');
    $("#camera_left").removeClass('active');
});

$( "#nextBtn" ).click(function() {
  input_index = (input_index+1)%total_file_num;
  document.getElementById("myVal").value = input_index;
  load(input_index);
});

$( "#prevBtn" ).click(function() {
    input_index = (input_index-1);
    if (input_index<0){ input_index = total_file_num-1; }
    document.getElementById("myVal").value = input_index;
    load(input_index);
});

$("#myVal").keyup(function(event) {
    if (event.keyCode === 13) {
        input_index = document.getElementById("myVal").value % total_file_num;
        document.getElementById("myVal").value = input_index;
        load(input_index);
    }
});

$('#gameSel').on('hidden.bs.select', function (e) {
  total_file_num = e.target.value;
  game_folder = $(this).find("option:selected").text();
  folder_path = '/static/imgs/'+game_folder+'/';
  annotation_path = '/static/annotation/'+game_folder+'/';

  // reset some things
  input_index = 0;
  img_index = 0;
  document.getElementById("myVal").value = input_index;
  load(input_index);

});

/*function handleClick(event){
    console.log(document.getElementById("myVal").value)
    input_index = document.getElementById("myVal").value;
    load(input_index);
*/

$( "#submitBtn" ).click(function() {
    $('#loading').show();
    // this is how the call to 'calibrate' in the app is made
    if (img_index ==num_pts_image){
        $.ajax({
            type: "GET",
            url: $SCRIPT_ROOT + "/calibrate/",
            contentType: "application/json; charset=utf-8",
            data:{img_anno:JSON.stringify(img_labels),ground_anno:JSON.stringify(ground_labels),input:input_index,camera_direction:camera_facing,game_folder:game_folder},
            success: function(data) {
                $('#loading').hide();
                d3.select('#my_image').attr("xlink:href","static/imgs/output/"+data.name)
            }
        });
        //console.log({img_anno:JSON.stringify(img_labels),ground_anno:JSON.stringify(ground_labels),input:input_index,camera_direction:camera_facing});
    }
    //console.log(data);
});

$( "#resetBtn" ).click(function() {
    reset_template()
    reset_image(input_index);
    $.ajax({
        type: "GET",
        url: $SCRIPT_ROOT + "/reset/",
        contentType: "application/json; charset=utf-8",
        data:{input:input_index,game_folder:game_folder}
    });
});

function load_template(ground_labels) {
  d3.selectAll(".pool_pts").remove();

  field_points = [
       {'x':ground_labels[0][0]+resolution,'y':ground_labels[0][1]+resolution,'id':'gt0','stroke':'blueviolet','fill':'blueviolet','opacity':0.65},
       {'x':ground_labels[1][0]+resolution,'y':ground_labels[1][1]+resolution,'id':'gt1','stroke':'dodgerblue','fill':'dodgerblue','opacity':0.65},
       {'x':ground_labels[2][0]+resolution,'y':ground_labels[2][1]+resolution,'id':'gt2','stroke':'cyan','fill':'cyan','opacity':0.65},
       {'x':ground_labels[3][0]+resolution,'y':ground_labels[3][1]+resolution,'id':'gt3','stroke':'orange','fill':'orange','opacity':0.65},
       {'x':ground_labels[4][0]+resolution,'y':ground_labels[4][1]+resolution,'id':'gt4','stroke':'green','fill':'green','opacity':0.65},
       {'x':ground_labels[5][0]+resolution,'y':ground_labels[5][1]+resolution,'id':'gt5','stroke':'fuchsia','fill':'fuchsia','opacity':0.65}
     ]


  var gt_circles = svg.selectAll('circle')
      .data(field_points)
      .enter().append('circle')
      .attr('cx', function(d) { return d.x; })
      .attr('cy', function(d) { return d.y; })
      .attr('fill', function(d) { return d.fill; })
      .attr('stroke', function(d) { return d.stroke; })
      .attr('fill-opacity', function(d) { return d.opacity; })
      .attr('id',function(d) { return d.id; })
      .attr('r', r)
      .attr('class','pool_pts')
      .call(drag);
  return ground_labels
}

function reset_template() {
  d3.selectAll(".pool_pts").remove();

  if (camera_facing=='left'){
    field_points = [
       {'x':13*resolution,'y':1*resolution,'id':'gt0','stroke':'blueviolet','fill':'blueviolet','opacity':0.65},
       {'x':7*resolution,'y':1*resolution,'id':'gt1','stroke':'dodgerblue','fill':'dodgerblue','opacity':0.65},
       {'x':3*resolution,'y':1*resolution,'id':'gt2','stroke':'cyan','fill':'cyan','opacity':0.65},
       {'x':3*resolution,'y':18*resolution,'id':'gt3','stroke':'orange','fill':'orange','opacity':0.65},
       {'x':3*resolution,'y':24*resolution,'id':'gt4','stroke':'green','fill':'green','opacity':0.65},
       {'x':13*resolution,'y':41*resolution,'id':'gt5','stroke':'fuchsia','fill':'fuchsia','opacity':0.65}
     ]
  } else {
    field_points = [
       {'x':(28*2-13)*resolution,'y':1*resolution,'id':'gt0','stroke':'blueviolet','fill':'blueviolet','opacity':0.65},
       {'x':(28*2-7)*resolution,'y':1*resolution,'id':'gt1','stroke':'dodgerblue','fill':'dodgerblue','opacity':0.65},
       {'x':(27*2-2+1)*resolution,'y':1*resolution,'id':'gt2','stroke':'cyan','fill':'cyan','opacity':0.65},
       {'x':(27*2-2+1)*resolution,'y':18*resolution,'id':'gt3','stroke':'orange','fill':'orange','opacity':0.65},
       {'x':(27*2-2+1)*resolution,'y':24*resolution,'id':'gt4','stroke':'green','fill':'green','opacity':0.65},
       {'x':(28*2-13)*resolution,'y':41*resolution,'id':'gt5','stroke':'fuchsia','fill':'fuchsia','opacity':0.65}
     ]
  }

  ground_labels = [];
  for(var i = 0;i<num_pts_ground;i++){
      ground_labels.push([field_points[i].x-resolution,field_points[i].y-resolution]);
  }
  //console.log(ground_labels)

  var gt_circles = svg.selectAll('circle')
      .data(field_points)
      .enter().append('circle')
      .attr('cx', function(d) { return d.x; })
      .attr('cy', function(d) { return d.y; })
      .attr('fill', function(d) { return d.fill; })
      .attr('stroke', function(d) { return d.stroke; })
      .attr('fill-opacity', function(d) { return d.opacity; })
      .attr('id',function(d) { return d.id; })
      .attr('r', r)
      .attr('class','pool_pts')
      .call(drag);
  return ground_labels
}

var svg = d3.select('body').append('svg')
    .attr('width',width)
    .attr('height',height);

svg.selectAll('.vertical')
    .data(d3.range(1, width / resolution))
    .enter().append('line')
    .attr('class', 'vertical')
    .attr('x1', function(d) { return d * resolution; })
    .attr('y1', 0)
    .attr('x2', function(d) { return d * resolution; })
    .attr('y2', height);

svg.selectAll('.horizontal')
    .data(d3.range(1, height / resolution))
    .enter().append('line')
    .attr('class', 'horizontal')
    .attr('x1', 0)
    .attr('y1', function(d) { return d * resolution; })
    .attr('x2', width)
    .attr('y2', function(d) { return d * resolution; });

var right_cage = svg.selectAll('rect')
   .data(pool_plane)
   .enter().append('rect')
   .classed('background',true)
   .attr('x',function(d) { return d.x*resolution})
   .attr('y',function(d) {return d.y*resolution})
   .attr('width',function(d) {return d.width*resolution})
   .attr('height',function(d) {return d.height*resolution})
   .attr('fill',function(d) {return d.fill});

var svg_image = d3.select("#img").append("svg")
          .attr("width",1920)
          .attr("height",1080)
          .on("click", function(){
              if (img_index<num_pts_image){
                  var coordinates = d3.mouse(svg_image.node());
                  img_labels[img_index][0] = coordinates[0];
                  img_labels[img_index][1] = coordinates[1];
                  img_index = img_index+1;
              }
          });

//console.log(JSON.parse("/Users/panna/Documents/scet/calibration_app_grid/static/annotation/0.json"));

function loadJSON(file, callback) {
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', file, true); // Replace 'my_data' with the path to your file
    xobj.onreadystatechange = function () {
          if (xobj.readyState == 4 && xobj.status == "200") {
            // Required use of an anonymous callback as .open will NOT return a value but simply returns undefined in asynchronous mode
            callback(xobj.responseText);
          }
    };
    xobj.send(null);
 }


function load(inputfile_index) {
    $.ajaxSetup({ cache:false });
    console.log('json file');
    console.log(annotation_path + inputfile_index + '.json');
    console.log('why does nothing happen?')
    $.getJSON(annotation_path + inputfile_index + '.json', function(data) {
        var actual_JSON = data; //JSON.parse(data);
        console.log("why don't we reach here??");
        console.log('loaded json');
        console.log(actual_JSON);
        if (actual_JSON.img_pts.length==num_pts_image) {
          camera_direction = actual_JSON.camera_direction;
          img_index = 0;
          if (camera_direction == 'left') {
            $("#camera_left").addClass('active');
            $("#camera_right").removeClass('active');
          } else {
            $("#camera_right").addClass('active');
            $("#camera_left").removeClass('active');
          }
          ground_labels = load_template(actual_JSON.template_pts);
          img_labels = load_image(input_index,actual_JSON.img_pts);
          img_index = num_pts_image;
          $( "#submitBtn" ).click();
          /*console.log('inside callback');
          console.log(img_labels);*/
        } else {
          ground_labels = reset_template();
          img_labels = reset_image(input_index);
        }
    });
    /*console.log('outside callback');
    console.log(img_labels);*/


}
console.log('loading..');
console.log(input_index);
load(input_index);

//reset_template();
//reset_image(input_index);

function load_image(inputfile_index,img_labels){
  svg_image.selectAll("image").remove();
  d3.selectAll(".image_pts").remove();
  /*img_labels = [];
  for(var i = 0;i<num_pts_image;i++){
      img_labels.push([0,0]);
  }*/
  //console.log(img_labels);
  //img_index = 0;
  svg_image.append("svg:image")
      .attr('width',1920)
      .attr("height",1080)
      .attr("id","my_image")
      .attr("xlink:href",folder_path + inputfile_index + '.png');

 image_points = [
         {'x':img_labels[0][0],'y':img_labels[0][1],'id':'im0','stroke':'blueviolet','fill':'blueviolet','opacity':0.65},
         {'x':img_labels[1][0],'y':img_labels[1][1],'id':'im1','stroke':'dodgerblue','fill':'dodgerblue','opacity':0.65},
         {'x':img_labels[2][0],'y':img_labels[2][1],'id':'im2','stroke':'cyan','fill':'cyan','opacity':0.65},
         {'x':img_labels[3][0],'y':img_labels[3][1],'id':'im3','stroke':'orange','fill':'orange','opacity':0.65},
         {'x':img_labels[4][0],'y':img_labels[4][1],'id':'im4','stroke':'green','fill':'green','opacity':0.65},
         {'x':img_labels[5][0],'y':img_labels[5][1],'id':'im5','stroke':'fuchsia','fill':'fuchsia','opacity':0.65},
         {'x':img_labels[6][0],'y':img_labels[6][1],'id':'im6','stroke':'black','fill':'black','opacity':0.65},
         {'x':img_labels[7][0],'y':img_labels[7][1],'id':'im7','stroke':'black','fill':'black','opacity':0.65}
       ]

  var image_circles =  svg_image.selectAll('circles')
      .data(image_points)
      .enter().append('circle')
      .attr("cx",function(d){return d.x})
      .attr("cy",function(d){return d.y})
      .attr("r",15)
      .attr("fill",function(d){return d.fill})
      .attr("id",function(d){return d.id})
      .attr('stroke', function(d) { return d.stroke; })
      .attr('fill-opacity', function(d) { return d.opacity; })
      .attr("class","image_pts")
      .call(drag_img);

   img_index = num_pts_image;
   //console.log(img_labels);
   return img_labels
}

function reset_image(inputfile_index){
  //console.log('we should never reach here');
  svg_image.selectAll("image").remove();
  d3.selectAll(".image_pts").remove();
  img_labels = [];
  for(var i = 0;i<num_pts_image;i++){
      img_labels.push([0,0]);
  }
  //console.log(img_labels);
  img_index = 0;
  svg_image.append("svg:image")
      .attr('width',1920)
      .attr("height",1080)
      .attr("id","my_image")
      .attr("xlink:href",folder_path + inputfile_index + '.png')
      .on("mousemove", function(){
              if (img_index<num_pts_image){
                  var coordinates = d3.mouse(svg_image.node());
                  d3.select('#im'+img_index)
                  .attr("cx",(coordinates[0]) + "px")
                  .attr("cy",(coordinates[1]) + "px");
              }
      })
      .on('click', function(){
        if (img_index == num_pts_image) {
          $( "#submitBtn" ).click();
        }
      });


 image_points = [
         {'x':100,'y':100,'id':'im0','stroke':'blueviolet','fill':'blueviolet','opacity':0.65},
         {'x':150,'y':100,'id':'im1','stroke':'dodgerblue','fill':'dodgerblue','opacity':0.65},
         {'x':200,'y':100,'id':'im2','stroke':'cyan','fill':'cyan','opacity':0.65},
         {'x':250,'y':100,'id':'im3','stroke':'orange','fill':'orange','opacity':0.65},
         {'x':300,'y':100,'id':'im4','stroke':'green','fill':'green','opacity':0.65},
         {'x':350,'y':100,'id':'im5','stroke':'fuchsia','fill':'fuchsia','opacity':0.65},
         {'x':400,'y':100,'id':'im6','stroke':'black','fill':'black','opacity':0.65},
         {'x':450,'y':100,'id':'im7','stroke':'black','fill':'black','opacity':0.65}
       ]

  //console.log("why don't these show up?")
  var image_circles =  svg_image.selectAll('circles')
      .data(image_points)
      .enter().append('circle')
      .attr("cx",function(d){return d.x})
      .attr("cy",function(d){return d.y})
      .attr("r",15)
      .attr("fill",function(d){return d.fill})
      .attr("id",function(d){return d.id})
      .attr('stroke', function(d) { return d.stroke; })
      .attr('fill-opacity', function(d) { return d.opacity; })
      .attr("class","image_pts")
      .call(drag_img);
  return img_labels
}

function dragged(d) {
  var x = d3.event.x,
      y = d3.event.y,
      gridX = round(Math.max(r, Math.min(width - r, x)), resolution),
      gridY = round(Math.max(r, Math.min(height - r, y)), resolution);

      d3.select(this).attr('cx', d.x = gridX).attr('cy', d.y = gridY);

      var idx = parseInt(d.id[2]);
      //console.log(ground_labels)
      ground_labels[idx][0] = gridX-resolution;
      ground_labels[idx][1] = gridY-resolution;
}

function round(p, n) {
    return p % n < n / 2 ? p - (p % n) : p + n - (p % n);
}
