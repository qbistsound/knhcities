Vue.component("cities-header", {
	template: "<nav class=\"bar clearfix\">" +
					"<a href=\"javascript:;\" class=\"brand\">" + 
						"<img class=\"logo\" src=\"/public/logo.png\" />" +
						"<span class=\"title\">K&H</span>" +
					"</a>" +
					 "<div class=\"menu search-menu\">" + 
						"<input placeholder=\"Search...\" type=\"text\" id=\"knh-cities-search-input\" v-on:keyup.enter=\"search()\" />" + 
					"</div>" +
					"<div class=\"load-container\">" +
						"<div class=\"loader\" v-if=\"$parent.loading\">LOADING</div>" +
					"</div>" +					
				"</nav>", 
	methods:
	{
		search: function()
		{
			var element = document.getElementById("knh-cities-search-input");
			if (element.value.trim().length <= 0) { this.$parent.download(); }
			if (element.value.trim().length > 2) { this.$parent.search(element.value.trim()); }
		}
	}
});
Vue.component("cities-thumbnails", {
	template: 	"<transition name=\"fade\">" +
					"<div class=\"container\" v-if=\"!$parent.loading\">" +
						"<div class=\"modal modal-window\">" + 
							"<input id=\"city-modal\" type=\"checkbox\" />" + 
							"<label for=\"city-modal\" class=\"overlay\"></label>" + 
							"<article v-if=\"$parent.records[selected]\">" + 
								"<header>" + 
									"<h3>{{ $parent.records[selected].name }}</h3>" + 
									//"<label for=\"city-modal\" class=\"close\" @click=\"unmodal()\">&times;</label>" +
								"</header>" +
								"<section class=\"content\">" +
									"<div class=\"modal-image\">" +
										"<img :src=\"'data:image/png;base64, ' + $parent.records[selected].image\">" +
									"</div>" + 
								"</section>" + 
								"<footer v-if=\"$parent.editable === true\">" + 
									"<a type=\"button\" href=\"javascript:;\" class=\"modal-button button\">" +
										"<input type=\"file\" class=\"file-upload\" @change=\"send($event)\" />" + 
										"<span>UPLOAD</span>" +
									"</a>" +
									"<a type=\"button\" href=\"javascript:;\" class=\"modal-button button\" @click=\"input()\">RENAME</a>" +
									"<input type=\"text\" id=\"knh-cities-name-input\" :value=\"$parent.records[selected].name\" style=\"display: none;\" @blur=\"update()\" v-on:keyup.enter=\"update()\" />" +
								"</footer>" +
							"</article>" + 
						"</div>" +
						"<div class=\"flex five\">" +
							"<div v-for=\"(record, index) in $parent.records\">" +
								"<div class=\"thumbnail stack\">" +
									"<img :src=\"'data:image/png;base64, ' + record.image\">" +
								"</div>" +
								"<label class=\"button stack\" @click=\"modal(index)\" for=\"city-modal\">{{ record.name }}</label>" +
							"</div>" +
						"</div>" + 
					"</div>" +
				"</transition>",
	data: function()
	{
		return {
			selected: null
		}
	},
	methods:
	{
		modal: function(index)
		{
			this.selected = index;
		},
		unmodal: function()
		{
			this.selected = null;
		},
		input: function()
		{
			var element = document.getElementById("knh-cities-name-input");
			element.style.display = "block";	
			element.focus();
		},
		update: function()
		{
			var element = document.getElementById("knh-cities-name-input");
			var self = this;
			var name_t = element.value;
			var record_id = this.$parent.records[this.selected].id;
			if (name_t === this.$parent.records[this.selected].name) 
			{
				element.style.display = "none"; 
				return; 
			}
			//
			this.$parent.rename(record_id, name_t, function(){ 
				element.style.display = "none";
				self.unmodal();		 
			});
		},
		send: function(event)
		{
			if (event.target.files.length > 0)
			{
				var self = this;
				var record_id = this.$parent.records[this.selected].id;
				this.$parent.upload(event.target.files[0], record_id, function(){ self.unmodal(); });
			}	
		}
	}
});
Vue.component("cities-footer", {
	template: 	"<div class=\"footer\">" +
					"<div class=\"pagination\" v-if=\"$parent.loading || $parent.paginate === false\">" +
						"<div class=\"'paginate'\">&nbsp;</div>" +
					"</div>" +
					"<div class=\"pagination\" v-if=\"!$parent.loading && $parent.paginate === true\">" +
						"<div :class=\"'paginate left-arrow' + (($parent.page === 1) ? ' disabled' : '')\" @click=\"previous()\">" +
							"<span>\u25c1</span>" +
						"</div>" +
						"<a v-for=\"(pagec, index) in $parent.pages\" v-if=\"pagec < ($parent.count / $parent.len)\" :class=\"'paginate number-box' + ((pagec === $parent.page) ? ' active' : '')\" @click=\"goto(pagec)\">" +
							"<span>{{ pagec }}</span>" +
						"</a>" +
						"<div class=\"paginate number-box disabled\">" +
							"<span>...</span>" +
						"</div>" +
						"<div :class=\"'paginate number-box' + (($parent.page === ($parent.count / $parent.len)) ? ' active' : '')\" @click=\"goto(parseInt(($parent.count / $parent.len)))\">" +
							"<span>{{ ($parent.count / $parent.len) }}</span>" +
						"</div>" +
						"<div :class=\"'paginate left-arrow' + (($parent.page === ($parent.count / $parent.len)) ? ' disabled' : '')\" @click=\"next()\">" +
							"<span>\u25b7</span>" +
						"</div>" +
					"</div>" +
				"</div>",
	methods:
	{
		goto: function(index) { return this.draw(index); },
		previous: function() { return this.draw(parseInt((((this.$parent.page - 1) <= 0) ? 1 : (this.$parent.page - 1)))); },
		next: function() { return this.draw(parseInt((((this.$parent.page + 1) > (this.$parent.count / this.$parent.len)) ? (this.$parent.count / this.$parent.len) : (this.$parent.page + 1)))); },
		draw: function(index)
		{
			this.$parent.records = {};
			this.$parent.page = index;
			this.$parent.bof = (this.$parent.len * this.$parent.page);
			this.$parent.download();	
		}
	}
	
});
Vue.component("cities", {
   	template: 	"<div class=\"u-full-width\">" +
					"<cities-header />" +
					"<cities-thumbnails />" +
					"<cities-footer />" +
				"</div>",
	data: function()
	{
		return {
			loading: false,
			records: {},
			bof: 1,
			len: 20,
			count: 1,
			page: 1,
			psize: 6,
			pages: [],
			paginate: false,
			editable: false,
			url: "/_cities",
			sruri: "/_search",
			rnuri: "/_rename",
			uluri: "/_upload"
		}
	},
	methods:
	{
		search: function(query, callback)
		{
			var self = this;
			this.loading = true;
			var xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function() { if (xhttp.readyState === 4) { setTimeout(function() { self.read(JSON.parse(xhttp.responseText), callback); }, 500); } }
			xhttp.open("POST", this.sruri, true);
			//
			var _data = new FormData();
			_data.append("_bof", this.bof);
			_data.append("_size", this.len);
			_data.append("_query", query);
			//
			xhttp.send(_data);	
		},
		rename: function(id, name, callback)
		{
			var self = this;
			this.loading = true;
			var xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function() { if (xhttp.readyState === 4) { setTimeout(function() { self.read(JSON.parse(xhttp.responseText), callback); }, 500); } }
			xhttp.open("POST", this.rnuri, true);
			//
			var _data = new FormData();
			_data.append("_bof", this.bof);
			_data.append("_size", this.len);
			_data.append("_name", name);
			_data.append("_id", id);	
			//
			xhttp.send(_data);	
		},
		upload: function(file, identity, callback)
		{
			var self = this;
			this.loading = true;
			var xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function() 
			{ 
				if (xhttp.readyState === 4) 
				{ 
					setTimeout(function() { self.read(JSON.parse(xhttp.responseText), callback); }, 500); 
				} 
			}
			xhttp.open("POST", this.uluri, true);
			//
			var _data = new FormData();
			_data.append("file", file);
			_data.append("_bof", this.bof);
			_data.append("_size", this.len);
			_data.append("_id", identity);
			//
			xhttp.send(_data);		
		},
		download: function()
		{
			var self = this;
			this.loading = true;
			var xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function() { if (xhttp.readyState === 4) { setTimeout(function() { self.read(JSON.parse(xhttp.responseText)); }, 500); } }
			xhttp.open("GET", this.url + "?_bof=" + this.bof + "&_size=" + this.len, true);
			xhttp.send();
		},
		read: function(docbuf, callback)
		{
			this.loading = false;
			this.records = docbuf.elements;
			this.count = docbuf.count;
			this.editable = docbuf.editable;
			this.paginate = docbuf.paginate;
			//
			this.pages = [];
			var __eof = parseInt((this.page < this.psize) ? this.psize : this.page + (this.psize / 2));
			var __bof = parseInt((this.page < this.psize) ? 1 : (__eof - this.psize));
			for (var index = __bof; index <= __eof; index++) { this.pages.push(parseInt(index)); }
			if (callback != null) { callback(); }
		}
	},
	created: function()
	{
		this.download();
	}
});
var vm = new Vue({ el: '#component' });